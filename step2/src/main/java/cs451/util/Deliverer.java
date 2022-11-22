package cs451.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import cs451.Constants;
import cs451.links.FairLossLinks;
import cs451.message.MessageBatch;

/*
 * Implements the deliverer part of the protocol.
 * Waits for byte arrays to be placed in the queue
 * and goes up the layers handling the delivering action
 */
public class Deliverer extends Thread {

    private FairLossLinks upper_layer;
    private ArrayBlockingQueue<byte[]> queue;
    private AtomicBoolean alive;
    private MessageBatch batch;
    private byte[] buffer;
    private int count;

    public Deliverer(FairLossLinks fll) {
        this.upper_layer = fll;
        this.queue = new ArrayBlockingQueue(Constants.UDP_DELIVERER_QUEUE_SIZE);
        this.alive = new AtomicBoolean(true);
        this.count = 0;
    }

    public void run() {
        try {
            while(this.alive.get()) {
                this.buffer = this.queue.poll(100, TimeUnit.MILLISECONDS);
                if (this.buffer != null) {
                    this.batch = new MessageBatch(this.buffer);
                    this.upper_layer.deliver(this.batch);
                    this.count += this.batch.size();
                }
            }    
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Deliverer thread stopped. ID:" + Thread.currentThread().getId());
            System.out.println("Delivered " + this.count + " many UDP packets.");

        }
    }

    public void scheduleDelivery(byte[] data) {
        try {
            while (!this.queue.offer(data, 100, TimeUnit.MILLISECONDS) && this.alive.get()) {
                System.out.println("Could not schedule a buffer to Deliverer within given time limit. Retrying.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	public void stop_() {
        this.alive.set(false);
	}
}
