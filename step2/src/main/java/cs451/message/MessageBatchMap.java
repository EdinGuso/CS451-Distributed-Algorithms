package cs451.message;

import java.util.List;
import java.util.HashMap;
import java.util.LinkedList;

import cs451.Host;
import cs451.message.Address;
import cs451.message.Message;
import cs451.message.MessageBatch;

public class MessageBatchMap {

    private HashMap<Address, MessageBatch> batch_map;
    private HashMap<Address, Boolean> early_send_map;
    private LinkedList<MessageBatch> eligible_batches;
    private LinkedList<Address> addresses;
    private Address temp_address;
    
    public MessageBatchMap(List<Host> hosts) {
        this.batch_map = new HashMap<Address, MessageBatch>();
        this.early_send_map = new HashMap<Address, Boolean>();
        this.addresses = new LinkedList<Address>();
        for (Host host : hosts) {
            this.temp_address = new Address(host.getIp(), host.getPort());
            this.batch_map.put(this.temp_address, new MessageBatch());
            this.early_send_map.put(this.temp_address, false);
            this.addresses.add(this.temp_address);
        }
        this.temp_address = null;
    }

    public void add(Message m) {
        this.batch_map.get(new Address(m)).add(m);
    }

    public List<MessageBatch> eligibleBatches() {
        this.eligible_batches = new LinkedList<MessageBatch>();
        for (Address address : addresses) {
            if (this.early_send_map.get(address) || this.batch_map.get(address).isFull()) {
                this.eligible_batches.add(this.batch_map.get(address));
            }
        }
        return this.eligible_batches;
    }

    public void setEarlySend() {
        for (Address address : addresses) {
            if (this.batch_map.get(address).size() != 0) {
                this.early_send_map.put(address, true);
            }
        }
    }

    public void resetEarlySend() {
        for (Address address : addresses) {
            this.early_send_map.put(address, false);
        }
    }

    public boolean anyEarlySend() {
        for (Address address : addresses) {
            if (this.early_send_map.get(address)) {
                return true;
            }
        }
        return false;
    }

    public boolean anyBatchIsFull() {
        for (Address address : addresses) {
            if (this.batch_map.get(address).isFull()) {
                return true;
            }
        }
        return false;
    }

    public boolean allBatchesAreEmpty() {
        for (Address address : addresses) {
            if (this.batch_map.get(address).size() != 0) {
                return false;
            }
        }
        return true;
    }

    public void clear() {
        for (Address address : addresses) {
            this.batch_map.get(address).clear();
        }
        this.batch_map.clear();
    }
}
