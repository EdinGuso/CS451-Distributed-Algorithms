package cs451.links;

import java.util.List;
import java.util.HashMap;

import cs451.Host;
import cs451.util.Scheduler;
import cs451.links.PerfectLinks;
import cs451.links.FairLossLinks;
import cs451.message.Message;
import cs451.message.MessageZip;
import cs451.message.MessageBatch;

/*
 * Implements the stubborn links part of the protocol.
 * Constructs the lower layer and scheduler for resending, and provides
 * functions to upper layer (pl) for starting, sending and stopping;
 * as well as a deliver function to lower layer (fll).
 */
public class StubbornLinks {

    private PerfectLinks upper_layer;
    private FairLossLinks lower_layer;
    private Scheduler scheduler;
    private int id;

    public StubbornLinks(int id, int port, List<Host> hosts, PerfectLinks pl) {
        this.upper_layer = pl;
        this.lower_layer = new FairLossLinks(port, hosts, this);
        this.scheduler = new Scheduler(hosts.size(), this);
        this.id = id;
    }

    public void send(Message m) {
        this.lower_layer.send(m); //send the message
        this.scheduler.scheduleMessage(m); //and schedule it for resending in case ack does not arrive
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

    public void deliver(MessageBatch batch) {
        if (batch.getId() == this.id) { //this is an ack
            for (Message m : batch.messages()) {
                scheduler.acknowledgeMessage(m); //acknowledge that message
            }
        }
        else { //it is not an ack
            this.sendAck(batch); //send acknowledgment to the original sender
            for (MessageZip m : batch.messageZips()) {
                this.upper_layer.deliver(m);
            }
        }
    }
}
