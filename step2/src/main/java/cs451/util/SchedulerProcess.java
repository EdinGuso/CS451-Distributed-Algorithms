package cs451.util;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import cs451.Host;
import cs451.links.StubbornLinks;
import cs451.message.Message;
import cs451.util.structures.RangeSet;

/*
 * Implements the scheduler for specific destination
 * for stubborn links. Allows the scheduler to schedule
 * messages for retransmissions and to removes messages whose
 * acknowledgements have been delivered. Divides the allocated
 * bandwitdh evenly among origins.
 */
public class SchedulerProcess {

    private StubbornLinks link;
    private ConcurrentHashMap<Byte, RangeSet> map;
    private HashMap<Byte, Integer> iterated;
    private HashMap<Byte, Host> hosts_map;
    private int rate_counter;
    private int num_messages_per_origin;
    private AtomicBoolean alive;
    
    public SchedulerProcess(HashMap<Byte, Host> hosts_map, StubbornLinks sl) {
        this.link = sl;
        this.map = new ConcurrentHashMap<Byte, RangeSet>(hosts_map.size());
        this.iterated = new HashMap<Byte, Integer>(hosts_map.size());
        this.hosts_map = hosts_map;
        this.alive = new AtomicBoolean(true);
        for (Byte origin : hosts_map.keySet()) {
            this.map.put(origin, new RangeSet());
            this.iterated.put(origin, 0);
        }
    }

    public int reSendMessages(int num_messages, byte source, byte dest) {
        this.rate_counter = 0;
        for (Byte origin : hosts_map.keySet()) {
            if (!this.alive.get()) break;
            this.num_messages_per_origin = Math.max(1, num_messages / hosts_map.size()); //ensures at least 1 messages are sent per (origin, dest pair)
            synchronized (this.map.get(origin)) {
                for (Integer seq : this.map.get(origin)) {
                    if (!this.alive.get()) break;
                    if (this.num_messages_per_origin <= 0) break;
                    if (this.iterated.get(origin) < seq) {
                        this.iterated.put(origin, seq);
                    }
                    else {
                        this.link.reSend(new Message(origin, source, dest, seq));
                        this.num_messages_per_origin--;
                        this.rate_counter++;
                    }
                }
            }
        }
        return this.rate_counter;
    }

    public void scheduleMessage(byte origin, int seq) {
        synchronized (this.map.get(origin)) {
            this.map.get(origin).add(seq);
        }
    }

    public boolean acknowledgeMessage(byte origin, int seq) {
        synchronized (this.map.get(origin)) {
            if (this.map.get(origin).contains(seq)) {
                this.map.get(origin).remove(seq);
                return true;
            }
        }
        return false;
    }
	
	public void stop_() {
        this.alive.set(false);
	}
}
