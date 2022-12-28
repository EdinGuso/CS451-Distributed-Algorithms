package cs451.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

import cs451.Host;
import cs451.Constants;
import cs451.util.Message;
import cs451.links.StubbornLinks;

/*
 * Implements the scheduler for stubborn links.
 * Allows the stubborn links to schedule messages for
 * retransmissions and to remove messages whose
 * acknowledgements have been delivered.
 */
public class Scheduler extends Thread {

    private StubbornLinks link;
    private ConcurrentHashMap<Message, Boolean> map; //ConcurrentHashSet is not implemented in standard Java libraries
    private AtomicInteger num_acks;
    private AtomicInteger rate_limiter;
    private int rate_limiter_local;
    private int rate_counter;
    private int sleep_timer;
    private double ratio;
    private AtomicBoolean need_notification;
    private AtomicBoolean alive;
    private int capacity;
    
    public Scheduler(int num_processes, StubbornLinks link) {
        this.link = link;
        this.map = new ConcurrentHashMap<Message, Boolean>();
        //params for backoff alg start
        this.num_acks = new AtomicInteger(Constants.SL_HASHSET_SIZE / (2 * num_processes));
        this.rate_limiter = new AtomicInteger(Constants.SL_HASHSET_SIZE / num_processes);
        this.rate_counter = 0;
        this.sleep_timer = 400;
        this.ratio = 0;
        //params for backoff alg end
        this.need_notification = new AtomicBoolean(false);
        this.alive = new AtomicBoolean(true);
        this.capacity = Constants.SL_HASHSET_SIZE;
    }

    public void run() {
        try {
            while(this.alive.get()) { //while stop_ function is not called
                Thread.sleep(this.sleep_timer);
                // BACKOFF ALGORITHM START
                // R = min(1, acks received / attempted sends)
                this.ratio = Math.min(((double) this.num_acks.getAndSet(0)) / this.rate_limiter.get(), 1); 
                // sends(t+1) = sends(t) * 0.75 + R * (MAX - 128) * 0.25 + 32; sends(0) = 8192/count(processes); sends = [128, MAX]; where MAX = 8192
                this.rate_limiter.set(this.rate_limiter.get() * 3 / 4 + ((int) (this.ratio * (Constants.SL_HASHSET_SIZE - 128) / 4)) + 32); 
                // sleep(t+1) = sleep(t) * 0.75 + (1 - R) * 1800 * 0.25 + 50; sleep(0) = 400; sleep = [200, 2000]
                this.sleep_timer = this.sleep_timer * 3 / 4 + (int) ((1.0 - this.ratio) * 450) + 50;
                // BACKOFF ALGORITHM END
                this.rate_counter = this.rate_limiter.get();
                for (Message m : this.map.keySet()) { //for every message that did not receive an acknowledgement
                    if (!this.alive.get()) break; //if stop_ still hasn't been called
                    if (this.rate_counter == 0) break; //if we exceded our backoff limit
                    if (!this.map.replace(m, false, true)) {
                        this.link.retrySend(m); //send the message to the lower layer
                        this.rate_counter--;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("SCHEDULER INTERRUPTED. ID:" + Thread.currentThread().getId());
        } finally {
            System.out.println("SCHEDULER STOPPED. ID:" + Thread.currentThread().getId());
            this.map.clear();
            System.out.println("Scheduler hashset stopped and cleared.");
        }
    }

    public void scheduleMessage(Message m) {
        try {
            if ((this.map.size() >= this.capacity || this.map.size() >= 2 * this.rate_limiter.get())  && this.alive.get()) { //if we don't have space in the allocated memory (this is used to avoid locking the queue unnecessarily)
                synchronized(this.map) { //we also use rate_limiter here to signal to the app to not oversend (as it gets stuck here)
                    while((this.map.size() >= this.capacity || this.map.size() >= 2 * this.rate_limiter.get()) && this.alive.get()) { //while we don't have space in the allocated memory
                        this.need_notification.set(true);
                        this.map.wait(3000); //wait 3000ms or until notified by scheduler
                        this.need_notification.set(false);
                    }
                }
            }
            if (this.map.size() < this.capacity) { //if we have capacity
                this.map.put(m, false); //add it to the set

            }   
        } catch (Exception e) {
            System.out.println("MAIN INTERRUPTED. ID:" + Thread.currentThread().getId());
        }
    }

    public void acknowledgeMessage(Message m) {
        if (this.map.containsKey(m)) { //if the message has not been acknowledged before
            this.map.remove(m); //acknowledge it
            this.num_acks.getAndIncrement();
            if (this.need_notification.get() && this.map.size() < this.rate_limiter.get()) { //if main is sleeping and if we need more messages
                synchronized(this.map) {
                    this.map.notifyAll(); //notify the scheduler schedulers to wake up
                }
            }
        }
    }
	
	public void stop_() {
        this.alive.set(false); //stops us from sending more messages
	}
}
