package cs451.broadcast;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import cs451.Host;
import cs451.app.Application;
import cs451.links.PerfectLinks;
import cs451.message.Message;
import cs451.message.MessageLocal;

/*
 * Implements the best effort broadcast part of the protocol.
 * Constructs the lower layer, and provides functions to
 * upper layer (urb) for starting, broadcasting and stopping;
 * as well as a deliver function to lower layer (pl).
 */
public class BestEffortBroadcast {

    private Application upper_layer;
    private PerfectLinks lower_layer;
    private HashMap<Byte, Host> hosts_map;
    private HashMap<Byte, AtomicInteger> sequence_numbers;
    private AtomicBoolean alive;
    private int my_id;

    public BestEffortBroadcast(HashMap<Byte, Host> hosts_map, Host self, Application app) {
        this.upper_layer = app;
        this.lower_layer = new PerfectLinks(hosts_map, self, this);
        this.hosts_map = hosts_map;
        this.sequence_numbers = new HashMap<Byte, AtomicInteger>(hosts_map.size()-1);
        this.alive = new AtomicBoolean(true);
        this.my_id = (byte) self.getId();
        for (byte dest : hosts_map.keySet()) {
            if (dest != this.my_id) {
                this.sequence_numbers.put(dest, new AtomicInteger(0));
            }
        }
    }

    /*
     * Appends destination to the messages and forwards the
     * send signal to lower layer. Prevents from sending messages
     * to itself.
     */
    public void broadcast(MessageLocal ml) {
        for (Byte dest : hosts_map.keySet()) {
            if (!this.alive.get()) {
                break;
            }
            if (dest != this.my_id) {
                this.lower_layer.send(new Message(ml, dest, this.sequence_numbers.get(dest).incrementAndGet()));
            }
        }
    }

    /*
     * 
     */
    public void send(MessageLocal ml, byte dest) {
        this.lower_layer.send(new Message(ml, dest, this.sequence_numbers.get(dest).incrementAndGet()));
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
