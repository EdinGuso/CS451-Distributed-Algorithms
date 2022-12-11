package cs451.links;

import java.util.HashMap;

import cs451.Host;
import cs451.util.Scheduler;
import cs451.links.PerfectLinks;
import cs451.links.FairLossLinks;
import cs451.message.Message;
import cs451.message.MessageBatch;

/*
 * Implements the stubborn links part of the protocol.
 * Constructs the lower layer and scheduler for resending, and provides
 * functions to upper layer (pl) for starting, sending and stopping;
 * reSend function to scheduler; sendAck function to itself;
 * as well as a deliver function to lower layer (fll).
 */
public class StubbornLinks {

    private PerfectLinks upper_layer;
    private FairLossLinks lower_layer;
    private Scheduler scheduler;
    private byte my_id;

    public StubbornLinks(HashMap<Byte, Host> hosts_map, Host self, PerfectLinks pl) {
        this.upper_layer = pl;
        this.lower_layer = new FairLossLinks(hosts_map, self, this);
        this.scheduler = new Scheduler(hosts_map, self, this);
        this.my_id = (byte) self.getId();
    }

    /*
     * Forwards the send signal to lower layer. Schedules the message
     * for resending in case ack is not received
     */
    public void send(Message m) {
        this.lower_layer.send(m);
        this.scheduler.scheduleMessage(m);
    }

    /*
     * Forwards the send signal to lower layer. (Used by scheduler).
     */
    public void reSend(Message m) {
        this.lower_layer.send(m);
    }

    /*
     * Forwards the send signal to lower layer. (Used by this.deliver).
     */
    public void sendAck(MessageBatch batch) {
        this.lower_layer.sendAck(batch);
    }

    /*
     * Forwards the start signal to lower layer and scheduler.
     */
    public void start() {
        this.lower_layer.start();
        this.scheduler.start();
    }

    /*
     * Forwards the stop signal to scheduler and lower layer.
     */
    public void stop_() {
        this.scheduler.stop_();
        try { this.scheduler.join(); } catch (Exception e) { e.printStackTrace(); }
        this.lower_layer.stop_();
    }

    /*
     * If the batch is an acknowledgment batch, sends acknowledgments
     * to the scheduler. Otherwise sends back an acknowledgment and
     * forwards the deliver signal to upper layer for each message
     * in the batch.
     */
    public void deliver(MessageBatch batch) {
        if (batch.getSource() == this.my_id) {
            for (Message m : batch.messages()) {
                scheduler.acknowledgeMessage(m);
            }
        }
        else {
            this.sendAck(batch);
            for (Message m : batch.messages()) {
                this.upper_layer.deliver(m);
            }
        }
    }
}
