package cs451.broadcast;

import java.util.List;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import cs451.Host;
import cs451.Constants;
import cs451.util.Broadcaster;
import cs451.util.DeliveredURB;
import cs451.message.MessageZip;
import cs451.message.MessageInitial;
import cs451.broadcast.BestEffortBroadcast;
import cs451.broadcast.FIFOBroadcast;

// CAN DELETE PENDING, MAYBE??
// WE CANNOT RELIABLE CONTROL THE FLOW OF MESSAGES, CAUSING THE Broadcaster TO FILL UP AND DEADLOCK THE SYSTEM (MAYBE TRY WITH NO SIZE LIMIT?)
// HOW TO IMPLEMENT FIFO?
// MIGHT NEED TO COMMUNICATE THE INFORMATION OF FAILED PROCESSES TO SCHEDULER SO THAT WE DON'T SPAM UNNECESSARY MESSAGES (PROBLEMATIC IF EPFD)
// MIGHT NEED TO FIGURE OUT HOW TO USE LESS SPACE IN PERFECT LINKS
// MIGHT CONSIDER OPTIMIZING CLIENT. AS SOON AS A BATCH IS READY IT IS BEING SENT
// (DONE) MIGHT CONSIDER GIVING EVEN MORE PRIORITY TO ACKS IN CLIENT

/* TO EDITTTT
 * Implements majority-ack uniform reliable broadcast part of the protocol.
 * Constructs the lower layer, and provides functions to
 * upper layer (fifob) for starting, broadcasting and stopping;
 * as well as a deliver function to lower layer (beb).
 */
public class UniformReliableBroadcast {

    private FIFOBroadcast upper_layer;
    private BestEffortBroadcast lower_layer;
    private Broadcaster broadcaster;
    private DeliveredURB delivered;
    private ConcurrentHashMap<MessageInitial, Boolean> pending;
    private ConcurrentHashMap<MessageInitial, ConcurrentHashMap<Integer, Boolean>> acks;
    private List<Host> hosts;
    private MessageInitial broadcasted;
    private MessageInitial received;
    private Host self;
    private AtomicInteger limit;
    private int s_count;
    private int d_count;

    public UniformReliableBroadcast(Host host, List<Host> hosts, FIFOBroadcast fifob) {
        this.upper_layer = fifob;
        this.lower_layer = new BestEffortBroadcast(host, hosts, this);
        this.broadcaster = new Broadcaster(hosts.size(), this);
        this.delivered = new DeliveredURB(hosts);
        this.pending = new ConcurrentHashMap<MessageInitial, Boolean>();
        this.acks = new ConcurrentHashMap<MessageInitial, ConcurrentHashMap<Integer, Boolean>>();
        this.hosts = hosts;
        this.self = host;
        this.limit = new AtomicInteger(Math.max(1, Constants.URB_HASHSET_SIZE / (hosts.size() * hosts.size())));
        this.s_count = 0;
        this.d_count = 0;
    }

    public void broadcast(MessageZip m) {
        while (m.getM() > this.limit.get()) {
            try { Thread.sleep(100); } catch (Exception e) { e.printStackTrace(); }
        }
        this.broadcasted = new MessageInitial(m.getOrigin(), m.getM());
        this.pending.put(this.broadcasted, false);
        this.acks.put(this.broadcasted, new ConcurrentHashMap<Integer, Boolean>());
        this.acks.get(this.broadcasted).put(m.getId(), false);
        this.lower_layer.broadcast(m); //and broadcast it!
        this.s_count++;
    }

    public void bebBroadcast(MessageZip m) {
        this.lower_layer.broadcast(m);
    }

    public void start() {
        this.lower_layer.start();
        this.broadcaster.start();
    }

    public void stop_() {
        this.lower_layer.stop_();
        this.broadcaster.stop_();
        this.broadcaster.interrupt();
        try { this.broadcaster.join(); } catch (Exception e) { System.out.println("Main got interrupted while waiting for scheduler to join."); } //should not happen
        System.out.println("Broadcasted " + this.s_count + " many URB messages.");
        System.out.println("Delivered " + this.d_count + " many URB messages.");
    }

    public void deliver(MessageZip m) {
        this.received = new MessageInitial(m.getOrigin(), m.getM());
        if (!this.delivered.contains(this.received) && !this.acks.containsKey(this.received)) { //if this is the first time we receive this message
            this.acks.put(this.received, new ConcurrentHashMap<Integer, Boolean>()); //create an entry for it
        }
        if (!this.delivered.contains(this.received)) { //if we manage to add the new id (i.e. first time adding that id)
            if (!this.acks.get(this.received).contains(m.getId())) {
                this.acks.get(this.received).put(m.getId(), false);
                if(this.canDeliver(this.received)) { //and if it is valid to deliver this message
                    this.delivered.add(this.received); //deliver it
                    this.pending.remove(this.received);
                    this.acks.remove(this.received);
                    this.upper_layer.deliver(this.received);
                    if (m.getOrigin() == this.self.getId()) { //if I delivered my own message
                        this.limit.getAndIncrement(); //I can sand some more
                    }
                    this.d_count++;
                    if(this.d_count % 1000 == 0){
                        System.out.println("Rebroadcaster is " + this.broadcaster.size() + " / " + Constants.URB_HASHSET_SIZE + " full!");
                    }
                }
            }
        }
        if (!this.delivered.contains(this.received) && !this.pending.containsKey(this.received)) { //if we haven't seend this msg before
            this.pending.put(this.received, false); //add it to the pending set
            //following if condition is only necessary when there are 3 processes and 1 of them crashes
            if (!this.acks.get(this.received).contains(m.getId())) {
                this.acks.get(this.received).put(m.getId(), false);
                if(this.canDeliver(this.received)) { //and if it is valid to deliver this message
                    this.delivered.add(this.received); //deliver it
                    this.pending.remove(this.received);
                    this.acks.remove(this.received);
                    this.upper_layer.deliver(this.received);
                    if (m.getOrigin() == this.self.getId()) { //if I delivered my own message
                        this.limit.getAndIncrement(); //I can sand some more
                    }
                    this.d_count++;
                }
            }
            this.broadcaster.scheduleReBroadcast(new MessageZip(this.self.getId(), m.getOrigin(), m.getM())); //and broadcast it
        }          
    }

    public boolean canDeliver(MessageInitial m) {
        return this.acks.get(m).size() > this.hosts.size() / 2;
    }
}
