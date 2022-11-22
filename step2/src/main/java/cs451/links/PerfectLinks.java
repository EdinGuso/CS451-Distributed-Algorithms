package cs451.links;

import java.util.HashMap;

import cs451.Host;
import cs451.links.StubbornLinks;
import cs451.message.Message;
import cs451.message.MessageLocal;
import cs451.broadcast.BestEffortBroadcast;

// WARNING!: For Project Step 2 implementation, we have scrapped
// perfect links totally. It is unnecessary to waste memory here
// as the same check can be done im URB for cheaper.

/*
 * Implements the perfect links part of the protocol.
 * Constructs the lower layer and set for storing delivered messages,
 * and provides functions to upper layer (beb) for starting, sending
 * and stopping; as well as a deliver function to lower layer (sl).
 */
public class PerfectLinks {

    private BestEffortBroadcast upper_layer;
    private StubbornLinks lower_layer;

    public PerfectLinks(HashMap<Byte, Host> hosts_map, Host self, BestEffortBroadcast beb) {
        this.upper_layer = beb;
        this.lower_layer = new StubbornLinks(hosts_map, self, this);
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
    public void deliver(MessageLocal ml) {
        this.upper_layer.deliver(ml); //otherwise deliver it
    }
}
