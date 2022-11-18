package cs451.util;

import java.util.concurrent.atomic.AtomicBoolean;

import cs451.Constants;
import cs451.udp.UDPServer;
import cs451.links.FairLossLinks;

/*
 * Implements the deliverer part of the protocol.
 * Waits for message batches to placed in the queue
 * and goes up the layers handling the delivering action
 */
public class Deliverer extends Thread {

    private FairLossLinks upper_layer;
    private UDPServer receiver;
    private AtomicBoolean alive;

    public Deliverer(FairLossLinks fll, UDPServer receiver) {
        this.upper_layer = fll;
        this.receiver = receiver;
        this.alive = new AtomicBoolean(true);
    }

    public void run() {
        try {
            while(this.alive.get()) { //while stop_ function is not called
                while (this.receiver.queue.size() != 0 && this.alive.get()) { //if the queue is not empty
                    this.upper_layer.deliver(this.receiver.queue.poll()); //take the first value and deliver it
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
            System.out.println("DELIVERER STOPPED. ID:" + Thread.currentThread().getId());
        }
    }
	
	public void stop_() {
        this.alive.set(false); //stops us from delivering further messages
	}
}
