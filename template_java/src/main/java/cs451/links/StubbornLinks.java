package cs451.links;

import java.util.List;
import java.util.HashMap;

import cs451.Host;
import cs451.util.Message;
import cs451.util.MessageZip;
import cs451.util.MessageBatch;
import cs451.util.MessageZipBatch;
import cs451.util.Scheduler;
import cs451.links.PerfectLinks;
import cs451.links.FairLossLinks;

/*
 * Implements the stubborn links part of the protocol.
 * Constructs the lower layer and scheduler for resending, and provides
 * functions to upper layer for starting, sending and stopping;
 * as well as a deliver function to lower layer.
 */
public class StubbornLinks {

    private PerfectLinks upper_layer;
    private FairLossLinks lower_layer;
    private Scheduler scheduler;
    private HashMap<Integer,Host> hosts_map;
    private Host sender;
    private int id;

    public StubbornLinks(List<Host> hosts, int id, int port, Host target, PerfectLinks link) {
        this.upper_layer = link;
        this.lower_layer = new FairLossLinks(port, this);
        this.scheduler = new Scheduler(target, hosts.size(), this);
        this.hosts_map = new HashMap<Integer,Host>();
        for (Host host : hosts) { //maps hosts into a hashmap for efficient search during acknowledgment sending
            hosts_map.put(host.getId(), host);
        }
        this.sender = null;
        this.id = id;
    }

    public void send(Message m) {
        this.lower_layer.send(m); //send the message
        this.scheduler.schedule_message(m); //and schedule it for resending in case ack does not arrive
    }

    public void retrySend(Message m) {
        this.lower_layer.send(m);
    }

    public void sendAck(MessageBatch batch) {
        this.lower_layer.sendAck(batch);
    }

    public void start() {
        this.lower_layer.start();
        this.scheduler.start();
    }

    public void stop_() {
        this.scheduler.stop_();
        this.scheduler.interrupt();
        try { this.scheduler.join(); } catch (Exception e) { System.out.println("Main got interrupted while waiting for scheduler to join."); } //should not happen
        this.lower_layer.stop_();
    }

    public void deliver(MessageZipBatch batch) {
        if (batch.msgs().get(0).getId() == this.id) { //this is an ack
            for (MessageZip m : batch.msgs()) {
                scheduler.acknowledge_message(m); //acknowledge that message
            }
        }
        else { //it is not an ack
            this.sender = this.hosts_map.get(batch.msgs().get(0).getId()); //get the sender
            this.sendAck(new MessageBatch(batch, this.sender.getIp(), this.sender.getPort())); //send acknowledgment to the original sender
            for (MessageZip m : batch.msgs()) {
                this.upper_layer.deliver(m);
            }
        }
    }
}
