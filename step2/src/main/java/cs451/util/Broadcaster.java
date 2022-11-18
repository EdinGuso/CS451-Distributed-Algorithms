package cs451.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import cs451.Constants;
import cs451.message.MessageZip;
import cs451.broadcast.UniformReliableBroadcast;

/* TO EDITTT
 * Implements the scheduler for stubborn links.
 * Allows the stubborn links to schedule messages for
 * retransmissions and to remove messages whose
 * acknowledgements have been delivered.
 */
public class Broadcaster extends Thread {

    private UniformReliableBroadcast urb;
    private ConcurrentHashMap<MessageZip, Boolean> rebroadcast;
    private AtomicBoolean alive;
    private int rate_counter;
    private int rebroadcast_capacity;

    public Broadcaster(int num_processes, UniformReliableBroadcast urb) {
        this.urb = urb;
        this.rebroadcast = new ConcurrentHashMap<MessageZip, Boolean>();
        this.alive = new AtomicBoolean(true);
        this.rebroadcast_capacity = Constants.URB_HASHSET_SIZE;
    }

    public void run() {
        try {
            while(this.alive.get()) { //while stop_ function is not called
                for (MessageZip m : this.rebroadcast.keySet()) { //for every message that needs to be rebroadcast
                    if (!this.alive.get()) break; //if stop_ still hasn't been called
                    this.urb.bebBroadcast(m); //send the message to the lower layer
                    this.rebroadcast.remove(m); //remove it
                    if (this.rebroadcast.size() < this.rebroadcast_capacity / 2) { //if deliverer is sleeping and if we need more messages
                        synchronized(this.rebroadcast) {
                            this.rebroadcast.notifyAll(); //notify the scheduler schedulers to wake up
                        }
                    }
                }
            }
            Thread.sleep(10);
        } catch (Exception e) {
            System.out.println("BROADCASTER INTERRUPTED. ID:" + Thread.currentThread().getId());
        } finally {
            System.out.println("BROADCASTER STOPPED. ID:" + Thread.currentThread().getId());
            this.rebroadcast.clear();
            System.out.println("Broadcaster hashset stopped and cleared.");
        }
    }

    public void scheduleReBroadcast(MessageZip m) {
        try {
            if (this.rebroadcast.size() >= this.rebroadcast_capacity  && this.alive.get()) { //if we don't have space in the allocated memory (this is used to avoid locking the queue unnecessarily)
                synchronized(this.rebroadcast) {
                    while(this.rebroadcast.size() >= this.rebroadcast_capacity && this.alive.get()) { //while we don't have space in the allocated memory
                        System.out.println("BROADCASTER BLOCKED FOR: ID-" + Thread.currentThread().getId());
                        this.rebroadcast.wait(1000); //wait 1000ms or until notified by broadcaster
                    }
                }
            }
            if (this.rebroadcast.size() < this.rebroadcast_capacity) { //if we have capacity
                this.rebroadcast.put(m, false); //add it to the set
            }   
        } catch (Exception e) {
            System.out.println("DELIVERER INTERRUPTED. ID:" + Thread.currentThread().getId());
        }
    }

    public int size() {
        return rebroadcast.size();
    }
	
	public void stop_() {
        this.alive.set(false); //stops us from sending more messages
	}
}
