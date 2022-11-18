package cs451.broadcast;

import java.util.List;
import java.util.HashMap;
import java.util.TreeSet;

import cs451.Host;
import cs451.app.Application;
import cs451.message.MessageZip;
import cs451.message.MessageInitial;
import cs451.broadcast.UniformReliableBroadcast;

/* TO EDITTTT
 * Implements majority-ack uniform reliable broadcast part of the protocol.
 * Constructs the lower layer, and provides functions to
 * upper layer (fifob) for starting, broadcasting and stopping;
 * as well as a deliver function to lower layer (beb).
 */
public class FIFOBroadcast {

    private Application upper_layer;
    private UniformReliableBroadcast lower_layer;
    private HashMap<Integer, TreeSet<Integer>> received;
    private HashMap<Integer, Integer> max_received;

    public FIFOBroadcast(Host host, List<Host> hosts, Application app) {
        this.upper_layer = app;
        this.lower_layer = new UniformReliableBroadcast(host, hosts, this);
        this.received = new HashMap<Integer, TreeSet<Integer>>();
        this.max_received = new HashMap<Integer, Integer>();
        for (Host h : hosts) {
            this.received.put(h.getId(), new TreeSet<Integer>());
            this.max_received.put(h.getId(), 0);
        }
    }

    public void broadcast(MessageZip m) {
        this.lower_layer.broadcast(m);
    }

    public void start() {
        this.lower_layer.start();
    }

    public void stop_() {
        this.lower_layer.stop_();
    }

    public void deliver(MessageInitial m) {
        if (m.getM() == this.max_received.get(m.getOrigin()) + 1) {
            this.max_received.put(m.getOrigin(),this.max_received.get(m.getOrigin())+1);
            this.upper_layer.deliver(m);
            while (!this.received.get(m.getOrigin()).isEmpty() && this.received.get(m.getOrigin()).first() == this.max_received.get(m.getOrigin()) + 1) {
                this.received.get(m.getOrigin()).pollFirst();
                this.max_received.put(m.getOrigin(),this.max_received.get(m.getOrigin())+1);
                this.upper_layer.deliver(new MessageInitial(m.getOrigin(), this.max_received.get(m.getOrigin())));
            }
        }
        else {
            this.received.get(m.getOrigin()).add(m.getM());
        }
    }
}
