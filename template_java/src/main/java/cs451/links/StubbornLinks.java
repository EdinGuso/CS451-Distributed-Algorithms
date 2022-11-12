package cs451.links;

import java.util.List;
import java.util.HashMap;

import cs451.util.Message;
import cs451.util.MessageBatch;
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
    private int id;

    public StubbornLinks(int id, int port, int num_processes, PerfectLinks link) {
        this.upper_layer = link;
        this.lower_layer = new FairLossLinks(port, this);
        this.scheduler = new Scheduler(num_processes, this);
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
            for (Message m : batch.messages()) {
                this.upper_layer.deliver(m);
            }
        }
    }
}
