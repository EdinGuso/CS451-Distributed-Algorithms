package cs451.broadcast;

import java.util.HashMap;
import java.util.TreeSet;

import cs451.Host;
import cs451.app.Application;
import cs451.util.ReceivedFIFOB;
import cs451.message.MessageOrigin;
import cs451.broadcast.UniformReliableBroadcast;
import cs451.util.structures.Range;

/*
 * Implements first-in first-out broadcast part of the protocol.
 * Constructs the lower layer, and provides functions to
 * upper layer (app) for starting, broadcasting and stopping;
 * as well as a deliver function to lower layer (beb).
 */
public class FIFOBroadcast {

    private Application upper_layer;
    private UniformReliableBroadcast lower_layer;
    private ReceivedFIFOB received;
    private Range range;

    public FIFOBroadcast(HashMap<Byte, Host> hosts_map, Host self, Application app) {
        this.upper_layer = app;
        this.lower_layer = new UniformReliableBroadcast(hosts_map, self, this);
        this.received = new ReceivedFIFOB(hosts_map);
    }

    /*
     * Forwards the broadcast signal to lower layer.
     */
    public void broadcast(int seq) {
        this.lower_layer.broadcast(seq);
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
     * Ensures that the messages delivered from the lower layer (URB)
     * are delivered to the upper layer (APP) in FIFO fashion.
     */
    public void deliver(MessageOrigin mo) {
        this.range = received.add(mo); //add the element and return the range of in-order elements
        if (this.range.getMin() == this.range.getMax()) { //if there are no in-order elements
            return; //do nothing
        }
        for (int i = this.range.getMin() + 1; i <= this.range.getMax(); i++) { //otherwise
            this.upper_layer.deliver(new MessageOrigin(mo.getOrigin(), i)); //keep delivering while we have in-order elements
        }
    }
}
