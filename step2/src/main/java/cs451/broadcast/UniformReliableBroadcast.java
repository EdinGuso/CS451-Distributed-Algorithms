package cs451.broadcast;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import cs451.Host;
import cs451.Constants;
import cs451.util.DeliveredURB;
import cs451.message.MessageLocal;
import cs451.message.MessageOrigin;
import cs451.broadcast.BestEffortBroadcast;
import cs451.broadcast.FIFOBroadcast;

/*
 * Implements majority-ack uniform reliable broadcast part of the protocol.
 * Constructs the lower layer, and provides functions to
 * upper layer (fifob) for starting, broadcasting and stopping;
 * as well as a deliver function to lower layer (beb).
 */
public class UniformReliableBroadcast {

    private FIFOBroadcast upper_layer;
    private BestEffortBroadcast lower_layer;
    private DeliveredURB delivered;
    private ConcurrentHashMap<MessageOrigin, boolean[]> acks;
    private HashMap<Byte, Host> hosts_map;
    private MessageLocal b_ml;
    private MessageLocal r_ml;
    private MessageOrigin b_mo;
    private MessageOrigin r_mo;
    private AtomicBoolean alive;
    private int gc_frequency;
    private int limit;
    private int num_acks;
    private int sleep_time;
    private byte my_id;
    private int s_count;
    private int d_count;


    public UniformReliableBroadcast(HashMap<Byte, Host> hosts_map, Host self, FIFOBroadcast fifob) {
        this.upper_layer = fifob;
        this.lower_layer = new BestEffortBroadcast(hosts_map, self, this);
        this.delivered = new DeliveredURB(hosts_map);
        this.acks = new ConcurrentHashMap<MessageOrigin, boolean[]>();
        this.hosts_map = hosts_map;
        this.alive = new AtomicBoolean(true);
        this.gc_frequency = (2048 * Constants.URB_SEND_LIMIT) / (hosts_map.size() * hosts_map.size() * hosts_map.size());
        this.sleep_time = 10 * this.hosts_map.size();
        this.my_id = (byte) self.getId();
        this.s_count = 0;
        this.d_count = 0;
        Constants.FLOW_CONTROL.init(hosts_map, this.my_id, 2000, 2, 500, 30); //hostsmap, myid, base_startup_time_ms, wiggle_const, wait_const_ms, queue_size
    }

    /*
     * Ensures flow control. Does not allow many broadcasts
     * before delivering some. If we are allowed to send more,
     * creates the necessary entries in URB protocol, appends
     * origin id and sender if to the sequence number, and
     * forwards the broadcast signal to lower layer.
     */
    public void broadcast(int seq) {
        this.limit = Constants.FLOW_CONTROL.getLimit();
        while (seq > this.limit && this.alive.get()) {
            try { Thread.sleep(this.sleep_time); } catch (Exception e) { e.printStackTrace(); }
            this.limit = Constants.FLOW_CONTROL.getLimit();
        }
        if (!this.alive.get()) return;
        this.b_ml = new MessageLocal(this.my_id, this.my_id, seq);
        this.b_mo = new MessageOrigin(b_ml);
        this.acks.put(this.b_mo, new boolean[this.hosts_map.size()]);
        this.acks.get(this.b_mo)[this.b_ml.getSourceInt() - 1] = true;
        this.lower_layer.broadcast(this.b_ml);
        this.s_count++;
    }

    /*
     * Forwards the start signal to lower layer.
     */
    public void start() {
        this.lower_layer.start();
    }

    /*
     * Prevents future broadcasts. Forwards the stop signal to
     * lower layer. Prints the stats.
     */
    public void stop_() {
        this.alive.set(false);
        this.lower_layer.stop_();
        System.out.println("Broadcasted " + this.s_count + " many URB messages.");
        System.out.println("Delivered " + this.d_count + " many URB messages.");
    }

    /*
     * Ensures that the messages delivered from the lower layer (BEB)
     * are delivered to the upper layer (FIFOB) only when the uniform
     * agreement property is satisfied.
     */
    public void deliver(MessageLocal ml) {
        this.r_mo = new MessageOrigin(ml);
        if (this.delivered.contains(this.r_mo)) {
            return;
        }
        if (!this.acks.containsKey(this.r_mo)) { //if this is the first time we receive this message
            this.acks.put(this.r_mo, new boolean[this.hosts_map.size()]);
            this.r_ml = new MessageLocal(ml.getOrigin(), this.my_id, ml.getSeq());
            this.lower_layer.broadcast(this.r_ml);
            this.acks.get(this.r_mo)[this.r_ml.getSourceInt() - 1] = true;
        }
        if (!this.acks.get(this.r_mo)[ml.getSourceInt() - 1]) { //if we have not acked from this sender before
            this.acks.get(this.r_mo)[ml.getSourceInt() - 1] = true;
            if(this.canDeliver(this.r_mo)) { //and if it is valid to deliver this message
                this.delivered.add(this.r_mo); //deliver it
                this.acks.remove(this.r_mo);
                this.upper_layer.deliver(this.r_mo);
                this.d_count++;
                if (this.d_count % this.gc_frequency == 0) {
                    System.gc(); //periodically call gc because java :)
                }
                Constants.MY_FLOW_RATE++;
            }
        }
    }

    /*
     * Returns whether we are allowed to deliver or not.
     */
    private boolean canDeliver(MessageOrigin mo) {
        this.num_acks = 0;
        for (int i = 0; i < hosts_map.size(); i++) {
            if (this.acks.get(mo)[i]) {
                this.num_acks++;
            }
        }
        return this.num_acks > this.hosts_map.size() / 2;
    }
}
