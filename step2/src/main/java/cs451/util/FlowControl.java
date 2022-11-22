package cs451.util;

import java.nio.ByteBuffer;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import cs451.Host;
import cs451.Constants;
import cs451.util.structures.ArrayQueue;

/*
 * Handles all of the flow control implementation.
 */
public class FlowControl {

    private ConcurrentHashMap<Byte, ArrayQueue> flows;
    private HashMap<Byte, Host> hosts_map;
    private int dead_processes;
    private long wait_time_ns;
    private long startup_time_ns;
    private long start_time;
    private long prev_time;
    private long cur_time;
    private int wiggle_value;
    private int cur;
    private int min;
    private int dest_id;
    private byte my_id;
    private boolean slow_start;
    private boolean epfl_start;
    
    public FlowControl() {
        this.flows = new ConcurrentHashMap<Byte, ArrayQueue>();
        this.slow_start = true;
        this.epfl_start = false;
    }

    public void init(HashMap<Byte, Host> hosts_map, byte my_id, long base_startup_time_ms, int wiggle_constant, long wait_time_ms, int queue_size) {
        this.hosts_map = hosts_map;
        this.dead_processes = 0;
        this.wait_time_ns = 1000000 * wait_time_ms;
        this.startup_time_ns = 1000000 * (base_startup_time_ms + 10 * this.hosts_map.size() * this.hosts_map.size());
        this.start_time = System.nanoTime();
        this.prev_time = System.nanoTime();
        this.wiggle_value = wiggle_constant * Constants.URB_SEND_LIMIT / (hosts_map.size() * hosts_map.size());
        this.my_id = my_id;
        for (byte id : hosts_map.keySet()) {
            if (id != my_id) {
                this.flows.put(id, new ArrayQueue(queue_size));
            }
        }
    }

    public void processFlowInfo(byte[] buffer) {
        synchronized (this.flows.get(buffer[1])) {
            this.flows.get(buffer[1]).add(ByteBuffer.wrap(buffer).getInt(2));
        }
    }

    public int getMin() {
        if (this.slow_start) {
            this.cur_time = System.nanoTime();
            if (this.cur_time > this.start_time + this.startup_time_ns) {
                this.slow_start = false;
            }
            return 0;
        }
        else {
            if (!this.epfl_start) {
                this.cur_time = System.nanoTime();
                if (this.cur_time > this.start_time + 2 * this.startup_time_ns) {
                    this.epfl_start = true;
                }
            }
            this.dead_processes = 0;
            this.min = Integer.MAX_VALUE;
            for (ArrayQueue array_queue : this.flows.values()) {
                synchronized (array_queue) {
                    this.cur = array_queue.getMax();
                    if (this.epfl_start && this.cur == Integer.MAX_VALUE) {
                        this.dead_processes++;
                    }
                    if (this.cur < this.min) {
                        this.min = this.cur;
                    }    
                }
            }
            return this.min;    
        }
    }

    public int getLimit() {
        return this.getMin() / (this.hosts_map.size() - this.dead_processes) + this.wiggle_value;
    }

    public void attemptToSendFlowInfo(byte[] buffer, DatagramPacket packet, DatagramSocket socket) throws Exception {
        if (this.slow_start) {
            return;
        }
        this.cur_time = System.nanoTime();
        if (this.prev_time + this.wait_time_ns < this.cur_time) {
            this.prev_time = this.cur_time;
            for (Host h : hosts_map.values()) {
                if (h.getId() != this.my_id) {
                    buffer = ByteBuffer.allocate(Constants.UDP_PACKET_SIZE).put((byte) 1).put(this.my_id).putInt(Constants.MY_FLOW_RATE).array();
                    packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(h.getIp()), h.getPort());
                    socket.send(packet);  
                }
            }
        }
    }
}
