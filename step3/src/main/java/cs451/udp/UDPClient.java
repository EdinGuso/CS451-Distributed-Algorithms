package cs451.udp;

import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import cs451.Host;
import cs451.Constants;
import cs451.message.Message;
import cs451.message.MessageBatch;

/*
 * Implements the sender part of the protocol.
 * Waits for messages to placed in the queue and
 * compiles them into batches and places them on the network.
 */
public class UDPClient extends Thread {

    private ConcurrentHashMap<Byte, ArrayBlockingQueue<Message>> queue_map;
    private ArrayBlockingQueue<MessageBatch> ack_queue;
    private HashMap<Byte, Host> hosts_map;
    private DatagramSocket socket;
    private DatagramPacket packet;
    private Host dest_host;
    private MessageBatch batch;
    private Message m;
    private AtomicBoolean alive;
    private byte[] buffer;
    private int count;
    private int ack_count;
    
    public UDPClient(HashMap<Byte, Host> hosts_map, Host self, DatagramSocket socket) {
        try {
            this.queue_map = new ConcurrentHashMap<Byte, ArrayBlockingQueue<Message>>(hosts_map.size()-1);
            this.ack_queue = new ArrayBlockingQueue<MessageBatch>(Constants.UDP_ACK_QUEUE_SIZE);
            this.hosts_map = hosts_map;
			this.socket = socket;
            this.alive = new AtomicBoolean(true);
            this.buffer = new byte[Constants.UDP_PACKET_SIZE];
            this.count = 0;
            this.ack_count = 0;
            for (Byte dest : hosts_map.keySet()) {
                if (dest != (byte) self.getId()) {
                    this.queue_map.put(dest, new ArrayBlockingQueue<Message>(Constants.UDP_SENDER_QUEUE_SIZE / hosts_map.size()));
                }
            }
		}
        catch (Exception e) {
			e.printStackTrace();
		}
    }

    public void run() {
        try {
            while(this.alive.get()) {
                // SCHEDULE ACKS FIRST
                do {
                    this.batch = this.ack_queue.poll(10, TimeUnit.MILLISECONDS);
                    if (this.batch != null) {
                        this.sendBatch(this.batch.getSource());
                        this.ack_count += this.batch.size();
                    }
                } while (this.batch != null && this.alive.get());

                // THEN MSGS
                this.batch = new MessageBatch();
                for (ArrayBlockingQueue<Message> dest_queue : queue_map.values()) {
                    if (!this.alive.get()) break;
                    do {
                        this.m = dest_queue.poll();
                        if (this.m != null) {
                            this.batch.add(this.m);
                            if (this.batch.isFull()) {
                                this.sendBatch(this.batch.getDest());
                                this.count += this.batch.size();
                                this.batch.clear();
                            }
                        }
                    } while(this.m != null && this.alive.get());

                    if (!this.batch.isEmpty()) {
                        this.sendBatch(this.batch.getDest());
                        this.count += this.batch.size();
                        this.batch.clear();
                    }
                }

                // THEN FLOW CONTROL
                Constants.FLOW_CONTROL.attemptToSendFlowInfo(this.buffer, this.packet, this.socket);
            }
		}
        catch (Exception e) {
            e.printStackTrace();
		}
        finally {
            System.out.println("Client thread stopped. ID:" + Thread.currentThread().getId());
            this.queue_map.clear();
            this.ack_queue.clear();
            System.out.println("Client queues cleared.");
            System.out.println("Sent " + this.count + " many UDP messages.");
            System.out.println("Sent " + this.ack_count + " many UDP acknowledgments.");
        }
    }

    private void sendBatch(byte dest) throws Exception {
        // System.out.println("Batch size is " + this.batch.size());
        this.buffer = this.batch.bytes();
        this.dest_host = this.hosts_map.get(dest);
        this.packet = new DatagramPacket(this.buffer, this.buffer.length, InetAddress.getByName(this.dest_host.getIp()), this.dest_host.getPort());
        this.socket.send(this.packet);
    }

    public void scheduleMessage(Message m) {
        try {
            while (!this.queue_map.get(m.getDest()).offer(m, 100, TimeUnit.MILLISECONDS) && this.alive.get()) {
                System.out.println("Could not schedule a message to UDP Client within given time limit. Retrying.");
            }    
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void scheduleAck(MessageBatch batch) {
        try {
            while (!this.ack_queue.offer(batch, 100, TimeUnit.MILLISECONDS) && this.alive.get()) {
                System.out.println("Could not schedule a acknowledgment to UDP Client within given time limit. Retrying.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	public void stop_() {
        this.alive.set(false);
	}
}
