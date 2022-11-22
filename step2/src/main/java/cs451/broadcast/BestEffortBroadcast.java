package cs451.broadcast;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import cs451.Host;
import cs451.links.PerfectLinks;
import cs451.message.Message;
import cs451.message.MessageLocal;
import cs451.broadcast.UniformReliableBroadcast;

/*
 * Implements the best effort broadcast part of the protocol.
 * Constructs the lower layer, and provides functions to
 * upper layer (urb) for starting, broadcasting and stopping;
 * as well as a deliver function to lower layer (pl).
 */
public class BestEffortBroadcast {

    private UniformReliableBroadcast upper_layer;
    private PerfectLinks lower_layer;
    private HashMap<Byte, Host> hosts_map;
    private AtomicBoolean alive;
    private int my_id;

    public BestEffortBroadcast(HashMap<Byte, Host> hosts_map, Host self, UniformReliableBroadcast urb) {
        this.upper_layer = urb;
        this.lower_layer = new PerfectLinks(hosts_map, self, this);
        this.hosts_map = hosts_map;
        this.alive = new AtomicBoolean(true);
        this.my_id = self.getId();
    }

    /*
     * Appends destination to the messages and forwards the
     * send signal to lower layer. Prevents from sending messages
     * to itself.
     */
    public void broadcast(MessageLocal ml) {
        for (Byte dest : hosts_map.keySet()) { //we need to send this message to all hosts
            if (!this.alive.get()) {
                break;
            }
            if (dest != this.my_id) { //for all hosts other than us
                this.lower_layer.send(new Message(ml, dest)); //send using perfect links
            }
        }
    }

    /*
     * Forwards the start signal to lower layer.
     */
    public void start() {
        this.lower_layer.start();
    }

    /*
     * Prevents future sends. Forwards the stop signal to
     * lower layer.
     */
    public void stop_() {
        this.alive.set(false);
        this.lower_layer.stop_();
    }

    /*
     * Forwards the deliver signal to upper layer.
     */
    public void deliver(MessageLocal ml) {
        this.upper_layer.deliver(ml);
    }
}
