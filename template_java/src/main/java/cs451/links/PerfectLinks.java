package cs451.links;

import java.util.List;
import java.util.HashSet;

import cs451.Host;
import cs451.app.Application;
import cs451.util.Message;
import cs451.util.MessageZip;
import cs451.links.StubbornLinks;

/*
 * Implements the perfect links part of the protocol.
 * Constructs the lower layer and set for storing delivered messages,
 * and provides functions to upper layer (app) for starting, sending
 * and stopping; as well as a deliver function to lower layer.
 */
public class PerfectLinks {

    private Application app;
    private StubbornLinks lower_layer;
    private HashSet<MessageZip> delivered;
    private List<Host> hosts;
    private int id;
    private int s_count;
    private int d_count;

    public PerfectLinks(List<Host> hosts, int id, int port, Host target, Application app) {
        this.app = app;
        this.lower_layer = new StubbornLinks(hosts, id, port, target, this);
        this.delivered = new HashSet<MessageZip>();
        this.hosts = hosts;
        this.id = id;
        this.s_count = 0;
        this.d_count = 0;
    }

    public void send(Message m) {
        this.lower_layer.send(m);
        this.s_count++;
    }

    public void start() {
        this.lower_layer.start();
    }

    public void stop_() {
        this.lower_layer.stop_();
        System.out.println("Sent " + this.s_count + " many PL packets.");
        System.out.println("Delivered " + this.d_count + " many PL packets.");
    }

    public void deliver(MessageZip m) {
        if (this.delivered.contains(m)) { //if we have already delivered this message
            return; //do nothing
        }
        this.delivered.add(m); //otherwise deliver it
        this.app.deliver(m); //and store it so that we don't deliver again
        this.d_count++;
    }
}
