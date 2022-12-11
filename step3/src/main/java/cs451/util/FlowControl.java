package cs451.util;

import java.nio.ByteBuffer;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import cs451.Host;
import cs451.Constants;

/*
 * Handles all of the flow control implementation.
 */
public class FlowControl {

    private ConcurrentHashMap<Byte, Integer> max_completed_las;
    private HashMap<Byte, Host> hosts_map;
    private long wait_time_ns;
    private long prev_time;
    private long cur_time;
    private byte my_id;
    
    public FlowControl() {
        this.max_completed_las = new ConcurrentHashMap<Byte, Integer>();
    }

    public void init(HashMap<Byte, Host> hosts_map, byte my_id, long wait_time_ms) {
        this.hosts_map = hosts_map;
        this.wait_time_ns = 1000000 * wait_time_ms;
        this.prev_time = System.nanoTime();
        this.my_id = my_id;
        for (byte id : hosts_map.keySet()) {
            this.max_completed_las.put(id, 0);
        }
    }

    public void updateMyInfo(int max_completed_la) {
        this.max_completed_las.put(this.my_id, max_completed_la);
    }

    public void processFlowInfo(byte[] buffer) {
        this.max_completed_las.put(buffer[1], ByteBuffer.wrap(buffer).getInt(2));
    }

    public int getMin() {
        return Collections.min(this.max_completed_las.values());
    }

    public int getMedian() {
        LinkedList<Integer> temp = new LinkedList<Integer>(this.max_completed_las.values());
        Collections.sort(temp);
        return temp.get(hosts_map.size() / 2);
    }

    public void attemptToSendFlowInfo(byte[] buffer, DatagramPacket packet, DatagramSocket socket) throws Exception {
        this.cur_time = System.nanoTime();
        if (this.prev_time + this.wait_time_ns < this.cur_time) {
            this.prev_time = this.cur_time;
            for (Host h : hosts_map.values()) {
                if (h.getId() != this.my_id) {
                    buffer = ByteBuffer.allocate(Constants.UDP_PACKET_SIZE).put((byte) 1).put(this.my_id).putInt(this.max_completed_las.get(this.my_id)).array();
                    packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(h.getIp()), h.getPort());
                    socket.send(packet);  
                }
            }
        }
    }
}
