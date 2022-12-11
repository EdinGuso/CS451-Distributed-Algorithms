package cs451.links;

import java.util.HashMap;

import cs451.Host;
import cs451.links.StubbornLinks;
import cs451.message.Message;
import cs451.message.MessageLocal;
import cs451.broadcast.BestEffortBroadcast;
import cs451.util.structures.RangeSet;

/*
 * Implements the perfect links part of the protocol.
 * Constructs the lower layer and set for storing delivered messages,
 * and provides functions to upper layer (beb) for starting, sending
 * and stopping; as well as a deliver function to lower layer (sl).
 */
public class PerfectLinks {

    private BestEffortBroadcast upper_layer;
    private StubbornLinks lower_layer;
    private HashMap<Byte, RangeSet> delivered;

    public PerfectLinks(HashMap<Byte, Host> hosts_map, Host self, BestEffortBroadcast beb) {
        this.upper_layer = beb;
        this.lower_layer = new StubbornLinks(hosts_map, self, this);
        this.delivered = new HashMap<Byte, RangeSet>(hosts_map.size()-1);
        for (Byte source : hosts_map.keySet()) {
            if (source != (byte) self.getId()) {
                this.delivered.put(source, new RangeSet());
            }
        }
    }

    /*
     * Forwards the send signal to lower layer.
     */
    public void send(Message m) {
        this.lower_layer.send(m);
    }

    /*
     * Forwards the start signal to lower layer.
     */
    public void start() {
        this.lower_layer.start();
    }

    /*
     * Forwards the stop signal to lower layer.
     */
    public void stop_() {
        this.lower_layer.stop_();
    }

    /*
     * Forwards the deliver signal to upper layer.
     */
    public void deliver(Message m) {
        if (this.delivered.get(m.getSource()).contains(m.getSeq())) {
            return;
        }
        this.upper_layer.deliver(new MessageLocal(m));
        this.delivered.get(m.getSource()).add(m.getSeq());
    }
}