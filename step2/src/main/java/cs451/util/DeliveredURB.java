package cs451.util;

import java.util.HashMap;

import cs451.Host;
import cs451.message.MessageOrigin;
import cs451.util.structures.RangeSet;

/*
 * Implements the delivered object for the URB protocol.
 */
public class DeliveredURB {

    private HashMap<Byte, RangeSet> delivered;
    
    public DeliveredURB(HashMap<Byte, Host> hosts_map) {
        this.delivered = new HashMap<Byte, RangeSet>();
        for (Byte id : hosts_map.keySet()) {
            this.delivered.put(id, new RangeSet());
        }
    }

    public void add(MessageOrigin mo) {
        this.delivered.get(mo.getOrigin()).add(mo.getSeq());
    }

    public boolean contains(MessageOrigin mo) {
        return this.delivered.get(mo.getOrigin()).contains(mo.getSeq());
    }
}
