package cs451.app;

import java.util.Arrays;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import cs451.Host;
import cs451.Constants;
import cs451.app.LatticeAgreement;
import cs451.message.MessageLocal;
import cs451.broadcast.BestEffortBroadcast;
import cs451.util.structures.RangeSet;

enum LatticeMessageType {
    PROPOSAL((byte) 0), ACK((byte) 1), NACK((byte) 2);

    private final byte value;

    private LatticeMessageType(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return this.value;
    }
}

/*
 * Creates BEB and handles multi-shot LA.
 */
public class Application {

    private BestEffortBroadcast lower_layer;
    private ConcurrentHashMap<Integer, LatticeAgreement> ms_la;
    private ConcurrentHashMap<Byte, ConcurrentHashMap<Integer, MessageLocal>> la_buffer;
    private ConcurrentHashMap<Integer, String> logs;
    private HashMap<Byte, Host> hosts_map;
    private Host self;
    private int num_proposals;
    private int max_active_las;
    private RangeSet completed_las;
    private int max_deleted_la;
    private int max_written_la;
    private BufferedReader reader;
    private PrintWriter writer;
    private String config_filename;
    private String output_filename;
    private byte my_id;
    private AtomicBoolean alive;

    public Application(HashMap<Byte, Host> hosts_map, Host self, String config_filename, String output_filename) {
        try {this.reader = new BufferedReader(new FileReader(config_filename)); } catch (Exception e) { e.printStackTrace(); }

        String params = "";
        try { params = this.reader.readLine().trim(); } catch (Exception e) { e.printStackTrace(); }
        int num_proposals = Integer.parseInt(params.split(" ")[0]);
        int distinct_values = Integer.parseInt(params.split(" ")[2]);

        this.setupConstants(hosts_map.size(), distinct_values);

        Constants.FLOW_CONTROL.init(hosts_map, (byte) self.getId(), 1000);

        this.lower_layer = new BestEffortBroadcast(hosts_map, self, this);
        this.ms_la = new ConcurrentHashMap<Integer, LatticeAgreement>(num_proposals);
        this.la_buffer = new ConcurrentHashMap<Byte, ConcurrentHashMap<Integer, MessageLocal>>(hosts_map.size()-1);
        this.logs = new ConcurrentHashMap<Integer, String>(num_proposals);
        this.hosts_map = hosts_map;
        this.self = self;
        this.num_proposals = num_proposals;
        this.max_active_las = Constants.APP_LOG_SIZE;
        this.completed_las = new RangeSet();
        this.max_deleted_la = 0;
        this.max_written_la = 0;
        this.config_filename = config_filename;
        this.output_filename = output_filename;
        this.my_id = (byte) self.getId();
        this.alive = new AtomicBoolean(true);

        for (byte source : hosts_map.keySet()) {
            if (source != this.my_id) {
                this.la_buffer.put(source, new ConcurrentHashMap<Integer, MessageLocal>());
            }
        }
    }

    public void setupConstants(int num_processes, int distinct_values) {
        Constants.NETWORK_HEADER_SIZE = 6; // network header: (1 + 1 + 4)
        Constants.LATTICE_PAYLOAD_SIZE = 13 + 4 * distinct_values; // lattice header: (4 + 4 + 1 + 4) + set size: (4 * distinct_values)
        Constants.UDP_MESSAGE_SIZE = Constants.NETWORK_HEADER_SIZE + Constants.LATTICE_PAYLOAD_SIZE;
        Constants.UDP_MESSAGE_LIMIT = 8; // 8 messages per batch
        Constants.UDP_PACKET_SIZE = Constants.UDP_MESSAGE_SIZE * Constants.UDP_MESSAGE_LIMIT + 4; // +4 to store an int indicating how many messages in this batch

        Constants.MESSAGE_CLASS_SIZE = 24 + 8 * (distinct_values / 2);
        Constants.MESSAGE_BATCH_CLASS_SIZE = 8 * Constants.MESSAGE_CLASS_SIZE + 16;
        Constants.RANGE_CLASS_SIZE = 24;
        Constants.RANGESET_CLASS_SIZE = 136; // expected value
        Constants.HASHMAP_BYTE_RANGESET_SIZE = 16 + (num_processes - 1) * (Constants.RANGESET_CLASS_SIZE + 16); // expected value
        Constants.SCHEDULER_PROCESS_CLASS_SIZE = Integer.MAX_VALUE; // unbounded value

        Constants.APP_LOG_SIZE = Math.max(1, 10000 / (num_processes * (int)(Math.sqrt(distinct_values))));
        Constants.SL_RESEND_LIMIT = (int) Math.pow(2,14);
        Constants.UDP_SENDER_QUEUE_SIZE = ((int) Math.pow(2,19)) / Constants.MESSAGE_CLASS_SIZE;
        Constants.UDP_ACK_QUEUE_SIZE = ((int) Math.pow(2,19)) / Constants.MESSAGE_BATCH_CLASS_SIZE;
        Constants.UDP_DELIVERER_QUEUE_SIZE = ((int) Math.pow(2,21)) / Constants.UDP_PACKET_SIZE;
    }

    public byte[] generatePayload(int lattice_number, int proposal_number, LatticeMessageType type, Set<Integer> value) {
        if (value != null) {
            ByteBuffer buf = ByteBuffer.allocate(Constants.LATTICE_PAYLOAD_SIZE).putInt(lattice_number).putInt(proposal_number).put(type.getValue()).putInt(value.size());
            for (int v : value) {
                buf.putInt(v);
            }
            return buf.array();
        }
        else {
            return ByteBuffer.allocate(Constants.LATTICE_PAYLOAD_SIZE).putInt(lattice_number).putInt(proposal_number).put(type.getValue()).array();
        }
    }

    public void propose(int lattice_number, int proposal_number, Set<Integer> value) {
        this.lower_layer.broadcast(new MessageLocal(this.my_id, this.generatePayload(lattice_number, proposal_number, LatticeMessageType.PROPOSAL, value)));
    }

    public void sendAck(int lattice_number, int proposal_number, byte dest) {
        this.lower_layer.send(new MessageLocal(this.my_id, generatePayload(lattice_number, proposal_number, LatticeMessageType.ACK, null)), dest);
    }

    public void sendNack(int lattice_number, int proposal_number, Set<Integer> value, byte dest) {
        this.lower_layer.send(new MessageLocal(this.my_id, generatePayload(lattice_number, proposal_number, LatticeMessageType.NACK, value)), dest);
    }

    public void decide(int lattice_number, Set<Integer> value) {
        String decision = "";
        for (int v : value) {
            decision = decision + Integer.toString(v) + " ";
        }
        this.logs.put(lattice_number, decision.trim());
        this.completed_las.add(lattice_number);

        Constants.FLOW_CONTROL.updateMyInfo(this.completed_las.maxOrdered());

        while (this.max_deleted_la < Constants.FLOW_CONTROL.getMin()) {
            this.max_deleted_la++;
            // System.out.println("Deleting Lattice Agreement No." + this.max_deleted_la);
            this.ms_la.remove(this.max_deleted_la);
            for (ConcurrentHashMap<Integer, MessageLocal> single_la_buffer : this.la_buffer.values()) {
                single_la_buffer.remove(this.max_deleted_la);
            }
        }

        if (this.completed_las.maxOrdered() >= this.max_written_la + Constants.APP_LOG_SIZE) {
            this.write();
        }
    }

    public void processBuffer(int i) {
        for (ConcurrentHashMap<Integer, MessageLocal> single_la_buffer : this.la_buffer.values()) {
            for (MessageLocal ml : single_la_buffer.values()) {
                ByteBuffer buf = ByteBuffer.wrap(ml.getM());
                int lattice_number = buf.getInt();
                if (lattice_number == i) {
                    int proposal_number = buf.getInt();
                    buf.get(); // get the type
                    Set<Integer> value = new ConcurrentHashMap<Integer, Boolean>().newKeySet();
                    int num_vs = buf.getInt();
                    for (int j = 0; j < num_vs; j++) {
                        value.add(buf.getInt());
                    }
                    this.ms_la.get(lattice_number).deliverProposal(proposal_number, value, ml.getSource());
                }
            }
        }
    }

    public void start() {
        this.lower_layer.start();

        for (int i = 1; i <= this.num_proposals; i++) {
            while (i > Constants.FLOW_CONTROL.getMedian() + this.max_active_las && this.alive.get()) {
                try { Thread.sleep(1000); } catch (Exception e) { e.printStackTrace(); }
            }
            if (!this.alive.get()) break;

            String input = "";
            try { input = this.reader.readLine().trim(); } catch (Exception e) { e.printStackTrace(); }
            Set<Integer> proposal = new ConcurrentHashMap<Integer, Boolean>().newKeySet();
            for (String v : input.split(" ")) {
                proposal.add(Integer.parseInt(v));
            }
            this.ms_la.put(i, new LatticeAgreement(this.hosts_map, this.self, i, this));
            this.processBuffer(i);
            this.ms_la.get(i).propose(proposal);
        }
    }

    public void stop_() {
        this.alive.set(false);
        this.lower_layer.stop_();

        this.write();
    }

    public void deliver(MessageLocal ml) {
        ByteBuffer buf = ByteBuffer.wrap(ml.getM());
        int lattice_number = buf.getInt();
        int proposal_number = buf.getInt();
        byte type = buf.get();
        if (!this.ms_la.containsKey(lattice_number)) {
            if (type == LatticeMessageType.PROPOSAL.getValue() && lattice_number > this.max_deleted_la) {
                this.la_buffer.get(ml.getSource()).put(lattice_number, ml);
            }
            return;
        }
        if (type == LatticeMessageType.ACK.getValue()) {
            this.ms_la.get(lattice_number).deliverAck(proposal_number);
            return;
        }
        Set<Integer> value = new ConcurrentHashMap<Integer, Boolean>().newKeySet();
        int num_vs = buf.getInt();
        for (int i = 0; i < num_vs; i++) {
            value.add(buf.getInt());
        }
        if (type == LatticeMessageType.PROPOSAL.getValue()) {
            this.ms_la.get(lattice_number).deliverProposal(proposal_number, value, ml.getSource());
            return;
        }
        if (type == LatticeMessageType.NACK.getValue()) {
            this.ms_la.get(lattice_number).deliverNack(proposal_number, value);
            return;
        }
    }

    /*
     * Writes the contents of logs into the output file.
     */
    public void write() {
        try {
            synchronized (this.logs) { //synchronize since main and deliverer may access concurrently
                int max_ordered = this.completed_las.maxOrdered();
                this.writer = new PrintWriter(new FileWriter(this.output_filename, true)); //open the file
                while (this.max_written_la < max_ordered) { //if there are more messages to be written
                    this.max_written_la++;
                    String decision = this.logs.remove(this.max_written_la);
                    this.writer.println(decision); //write it
                }
                this.writer.close(); //close the file
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
