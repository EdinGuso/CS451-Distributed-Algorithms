package cs451.util;

import java.util.List;
import java.util.HashMap;

import cs451.Host;
import cs451.util.DeliveredZip;
import cs451.message.MessageIdentifier;

public class DeliveredPL {

    private HashMap<MessageIdentifier, DeliveredZip> delivered;
    private int size;
    private int count; //TO DELETE
    
    public DeliveredPL(List<Host> hosts) {
        this.delivered = new HashMap<MessageIdentifier, DeliveredZip>();
        for (Host host1 : hosts) {
            for (Host host2 : hosts) {
                this.delivered.put(new MessageIdentifier(host1.getId(), host2.getId()), new DeliveredZip());
            }
        }
        this.size = 0;
        this.count = 0; //TO DELETE
    }

    public void add(int id, int origin, int m) {
        this.count++; //TO DELETE
        this.delivered.get(new MessageIdentifier(id, origin)).add(m);
        if (this.count % 1000 == 0) { //TO DELETE
            System.out.println("PL => SIZE AFTER " + this.count + "th INSERTION: " + this.size());
        }
    }

    public boolean contains(int id, int origin, int m) {
        return this.delivered.get(new MessageIdentifier(id, origin)).contains(m);
    }

    public int size() {
        this.size = 0;
        for (DeliveredZip delivered_zip : delivered.values()) {
            this.size += delivered_zip.size();
        }
        return this.size;
    }
}
