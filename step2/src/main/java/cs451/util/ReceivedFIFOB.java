package cs451.util;

import java.util.HashMap;

import cs451.Host;
import cs451.message.MessageOrigin;
import cs451.util.structures.Range;
import cs451.util.structures.RangeSet;

/*
 * Handles the backend computation of FIFO property.
 */
public class ReceivedFIFOB {

    private HashMap<Byte, RangeSet> received;
    private int prev_ordered;
    
    public ReceivedFIFOB(HashMap<Byte, Host> hosts_map) {
        this.received = new HashMap<Byte, RangeSet>();
        for (Byte origin : hosts_map.keySet()) {
            this.received.put(origin, new RangeSet());
        }
    }

    public Range add(MessageOrigin mo) {
        this.prev_ordered = this.received.get(mo.getOrigin()).maxOrdered();
        this.received.get(mo.getOrigin()).add(mo.getSeq());
        return new Range(this.prev_ordered, this.received.get(mo.getOrigin()).maxOrdered());
    }

    public boolean contains(MessageOrigin mo) {
        return this.received.get(mo.getOrigin()).contains(mo.getSeq());
    }
}
