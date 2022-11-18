package cs451.util;

import java.util.List;
import java.util.HashMap;

import cs451.Host;
import cs451.util.DeliveredZip;
import cs451.message.MessageInitial;

public class DeliveredURB {

    private HashMap<Integer, DeliveredZip> delivered;
    private int size;
    private int count; //TO DELETE
    
    public DeliveredURB(List<Host> hosts) {
        this.delivered = new HashMap<Integer, DeliveredZip>();
        for (Host host : hosts) {
            this.delivered.put(host.getId(), new DeliveredZip());
        }
        this.size = 0;
        this.count = 0; //TO DELETE
    }

    public void add(MessageInitial m) {
        this.count++; //TO DELETE
        this.delivered.get(m.getOrigin()).add(m.getM());
        if (this.count % 1000 == 0) { //TO DELETE
            System.out.println("URB => SIZE AFTER " + this.count + "th INSERTION: " + this.size());
        }
    }

    public boolean contains(MessageInitial m) {
        return this.delivered.get(m.getOrigin()).contains(m.getM());
    }

    public int size() {
        this.size = 0;
        for (DeliveredZip delivered_zip : delivered.values()) {
            this.size += delivered_zip.size();
        }
        return this.size;
    }
}
