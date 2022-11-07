package cs451.util;

import java.util.concurrent.atomic.AtomicBoolean;

import cs451.Constants;
import cs451.udp.UDPServer;
import cs451.util.MessageZipBatch;
import cs451.links.FairLossLinks;

/*
 * Implements the deliverer part of the protocol.
 * Waits for message batches to placed in the queue
 * and goes up the layers handling the delivering action
 */
public class Deliverer extends Thread {

    private FairLossLinks upper_layer;
    private UDPServer receiver;
    private MessageZipBatch batch;
    private AtomicBoolean alive;
    private int count;

    public Deliverer(FairLossLinks link, UDPServer receiver) {
        this.upper_layer = link;
        this.receiver = receiver;
        this.alive = new AtomicBoolean(true);
        this.count = 0;
    }

    public void run() {
        try {
            while(this.alive.get()) { //while stop_ function is not called
                while (this.receiver.queue.size() != 0 && this.alive.get()) { //if the queue is not empty
                    this.batch = new MessageZipBatch(this.receiver.queue.poll()); //take the first value
                    this.upper_layer.deliver(this.batch);
                    this.count = this.count + this.batch.size();
                    if (this.receiver.queue.remainingCapacity() == Constants.UDP_DELIVERER_QUEUE_SIZE / 16) { //if we have used some objects
                        synchronized(this.receiver.queue) {
                            this.receiver.queue.notifyAll(); //notify the receiver to wake up
                        }
                    }
                }
                if (this.receiver.queue.size() == 0 && this.alive.get()) { //if there are no items to deliver (this is used to avoid locking the queue unnecessarily)
                    synchronized(this.receiver.queue) {
                        while(this.receiver.queue.size() == 0) { //while there are no items to deliver
                            this.receiver.queue.wait(100); //wait 100ms or until notified by receiver
                        }
                    }
                }
            }    
        } catch (Exception e) {
            System.out.println("DELIVERER INTERRUPTED. ID:" + Thread.currentThread().getId());
        } finally {
            System.out.println("Delivered " + this.count + " many UDP packets. ID:" + Thread.currentThread().getId());
        }
    }
	
	public void stop_() {
        this.alive.set(false); //stops us from delivering further messages
	}
}
