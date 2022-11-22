package cs451.util;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

import cs451.Host;
import cs451.Constants;
import cs451.util.SchedulerProcess;
import cs451.links.StubbornLinks;
import cs451.message.Message;

/*
 * Implements the scheduler for stubborn links.
 * Allows the stubborn links to schedule messages for
 * retransmissions and to removes messages whose
 * acknowledgements have been delivered. Uses an exponential
 * backoff algorithm for flow control. Divides the allocated
 * bandwitdh evenly among destinations.
 */
public class Scheduler extends Thread {

    private ConcurrentHashMap<Byte, SchedulerProcess> map;
    private HashMap<Byte, Host> hosts_map;
    private AtomicInteger num_acks;
    private double ratio;
    private int rate_limiter;
    private int sleep_timer;
    private AtomicBoolean alive;
    private byte my_id;
    private int count;
    
    public Scheduler(HashMap<Byte, Host> hosts_map, Host self, StubbornLinks sl) {
        this.map = new ConcurrentHashMap<Byte, SchedulerProcess>(hosts_map.size());
        this.hosts_map = hosts_map;
        //params for backoff alg start
        this.num_acks = new AtomicInteger(0);
        this.ratio = 0;
        this.rate_limiter = Constants.SL_RESEND_LIMIT / 4;
        this.sleep_timer = 800;
        //params for backoff alg end
        this.alive = new AtomicBoolean(true);
        this.my_id = (byte) self.getId();
        this.count = 0;
        for (Byte dest : hosts_map.keySet()) {
            this.map.put(dest, new SchedulerProcess(hosts_map, sl));
        }
    }

    public void run() {
        try {
            while(this.alive.get()) { //while stop_ function is not called
                Thread.sleep(this.sleep_timer);
                // BACKOFF ALGORITHM START
                // R = min(1, acks received / attempted sends)
                this.ratio = Math.min(((double) this.num_acks.getAndSet(0)) / this.rate_limiter, 1); 
                // sends(t+1) = sends(t) * 0.75 + R * (MAX - 1024) * 0.25 + 256; sends(0) = 16384/count(processes)^2; sends = [1024, MAX]; where MAX = 16384
                this.rate_limiter = this.rate_limiter * 3 / 4 + ((int) (this.ratio * (Constants.SL_RESEND_LIMIT - 1024) / 4)) + 256; 
                // sleep(t+1) = sleep(t) * 0.75 + (1 - R) * 1800 * 0.25 + 50; sleep(0) = 800; sleep = [200, 2000]
                this.sleep_timer = this.sleep_timer * 3 / 4 + (int) ((1.0 - this.ratio) * 450) + 50;
                // BACKOFF ALGORITHM END
                for (Byte dest : hosts_map.keySet()) {
                    if (!this.alive.get()) break; //if stop_ still hasn't been called
                    this.count += this.map.get(dest).reSendMessages(this.rate_limiter / this.hosts_map.size(), my_id, dest);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Scheduler thread stopped. ID:" + Thread.currentThread().getId());
            this.map.clear();
            System.out.println("Scheduler hashset stopped and cleared.");
            System.out.println("Rescheduled " + this.count + " many messages");
        }
    }

    public void scheduleMessage(Message m) {
        this.map.get(m.getDest()).scheduleMessage(m.getOrigin(), m.getSeq());
    }

    public void acknowledgeMessage(Message m) {
        if (this.map.get(m.getDest()).acknowledgeMessage(m.getOrigin(), m.getSeq())) {
            this.num_acks.getAndIncrement();
        }        
    }
	
	public void stop_() {
        this.alive.set(false);
        for (Byte dest : hosts_map.keySet()) {
            this.map.get(dest).stop_();
        }
	}
}
