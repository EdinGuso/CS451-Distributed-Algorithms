package cs451.broadcast;

import java.util.List;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

import cs451.Host;
import cs451.links.PerfectLinks;
import cs451.message.Message;
import cs451.message.MessageZip;
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
    private List<Host> hosts;
    private Host self;
    private int s_count;
    private int d_count;
    private AtomicBoolean alive;

    public BestEffortBroadcast(Host host, List<Host> hosts, UniformReliableBroadcast urb) {
        this.upper_layer = urb;
        this.lower_layer = new PerfectLinks(host.getId(), host.getPort(), hosts, this);
        this.hosts = hosts;
        this.self = host;
        this.alive = new AtomicBoolean(true);
    }

    public void broadcast(MessageZip m) {
        for (Host host : hosts) { //we need to send this message to all hosts
            if (!this.alive.get()) {
                break;
            }
            if (host.getId() != this.self.getId()) { //for all hosts other than us
                this.lower_layer.send(new Message(host.getIp(), host.getPort(), m.getId(), m.getOrigin(), m.getM())); //send using perfect links
            }
        }
    }

    public void start() {
        this.lower_layer.start();
    }

    public void stop_() {
        this.alive.set(false);
        this.lower_layer.stop_();
    }

    public void deliver(MessageZip m) {
        this.upper_layer.deliver(m);
    }
}
