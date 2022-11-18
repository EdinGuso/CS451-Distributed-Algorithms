package cs451.udp;

import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import cs451.Constants;
import cs451.util.Message;
import cs451.util.MessageBatch;

/*
 * Implements the sender part of the protocol.
 * Waits for messages to placed in the queue and
 * compiles them into batches and places them on the network.
 */
public class UDPClient extends Thread {

    private DatagramSocket socket;
    private DatagramPacket packet;
    private ArrayBlockingQueue<Message> queue;
    private ArrayBlockingQueue<MessageBatch> ack_queue;
    private AtomicBoolean alive;
    private MessageBatch batch;
    private MessageBatch ack_batch;
    private byte[] buffer;
    private boolean early_send;
    private int count;
    
    public UDPClient(DatagramSocket socket) {
        try {
			this.socket = socket;
            this.queue = new ArrayBlockingQueue(Constants.UDP_SENDER_QUEUE_SIZE); //limits the max size of the delivery queue
            this.ack_queue = new ArrayBlockingQueue(Constants.UDP_ACK_QUEUE_SIZE); //limits the max size of the delivery queue
            this.alive = new AtomicBoolean(true);
            this.batch = new MessageBatch();
            this.buffer = new byte[Constants.UDP_PACKET_SIZE];
            this.early_send = false;
            this.count = 0;
		}
        catch (Exception e) {
			e.printStackTrace();
		}
    }

    public void run() {
        try {
            while(this.alive.get()) { //while stop_ function is not called
                // SCHEDULE ACKS FIRST
                while (this.ack_queue.peek() != null && this.alive.get()) { //if the queue is not empty
                    this.ack_batch = this.ack_queue.poll();
                    if (this.ack_queue.remainingCapacity() == Constants.UDP_ACK_QUEUE_SIZE / 8) { //if we have used some objects
                        synchronized(this.ack_queue) {
                            this.ack_queue.notifyAll(); //notify the send scheduler to wake up
                        }
                    }
                    this.buffer = this.ack_batch.bytes();
                    this.packet = new DatagramPacket(this.buffer, this.buffer.length, InetAddress.getByName(this.ack_batch.getIp()), this.ack_batch.getPort()); //generate the datagram packet
                    this.socket.send(this.packet); //send the datagram packet
                    this.count = this.count + this.ack_batch.size();
                }

                // THEN SCHEDULE MSGS
                while ((this.queue.peek() != null || this.early_send) && this.alive.get()) { //if the queue is not empty or if we have promised an early send
                    while (this.queue.peek() != null && !this.batch.isFull()) { //if the queue is not empty and we still have space in this batch
                        this.batch.add(this.queue.poll()); //then add the first message in the queue to the batch
                        if (this.queue.remainingCapacity() == Constants.UDP_SENDER_QUEUE_SIZE / 8) { //if we have used some objects
                            synchronized(this.queue) {
                                this.queue.notifyAll(); //notify the send schedulers to wake up
                            }
                        }
                    }
                    if (this.batch.isFull() || (this.early_send && this.queue.peek() == null)) { //if the batch is full or (we have promised an early send and there are no messages waiting in the queue)
                        this.buffer = this.batch.bytes();
                        this.packet = new DatagramPacket(this.buffer, this.buffer.length, InetAddress.getByName(this.batch.getIp()), this.batch.getPort()); //generate the datagram packet
                        this.socket.send(this.packet); //send the datagram packet
                        this.count = this.count + this.batch.size();
                        this.batch.clear(); //clear the batch
                        this.early_send = false; //reset the early send flag
                    }
                }
                if (this.batch.size() > 0) { //if there are items in the batch
                    this.early_send = true; //promise an early send
                }

                // IF BOTH ARE EMPTY, SLEEP
                // We don't use signals because signals might come from 2 sources!
                if (this.queue.size() == 0 && this.ack_queue.size() == 0 && !this.early_send && this.alive.get()) { //if there is nothing to send, and we have not promised an early send
                    Thread.sleep(10);
                }
            }
		}
        catch (Exception e) {
            System.out.println("CLIENT INTERRUPTED. ID:" + Thread.currentThread().getId());
		}
        finally {
            System.out.println("CLIENT STOPPED. ID:" + Thread.currentThread().getId());
            this.queue.clear();
            this.ack_queue.clear();
            System.out.println("Sender queue cleared.");
            System.out.println("Sent " + this.count + " many UDP packets. ID:" + Thread.currentThread().getId());
        }
        
    }

    public void scheduleMessage(Message m) {
        try {
            if (this.queue.remainingCapacity() == 0 && this.alive.get()) { //if we don't have space in the allocated memory (this is used to avoid locking the queue unnecessarily)
                synchronized(this.queue) {
                    while(this.queue.remainingCapacity() == 0 && this.alive.get()) { //while we don't have space in the allocated memory
                        System.out.println("SEND SCHEDULER WAITS. ID:" + Thread.currentThread().getId());
                        this.queue.wait(200); //wait 200ms or until notified by sender
                    }
                }
            }
            if (this.queue.remainingCapacity() < 2) {  //if we have less than 2 space left we need to synchronize it
                synchronized(this.queue) { //since there are 2 threads (main, scheduler) that will be accessing this
                    if (this.queue.remainingCapacity() > 0) {
                        this.queue.add(m); //add it to the schedule
                    }
                }
            }
            else { //in other cases no synchronization is required
                this.queue.add(m); //add it to the schedule
            }            
        } catch (Exception e) {
            System.out.println("SEND SCHEDULER INTERRUPTED. ID:" + Thread.currentThread().getId());
        }
    }

    public void scheduleAck(MessageBatch batch) {
        try {
            if (this.ack_queue.remainingCapacity() == 0 && this.alive.get()) { //if we don't have space in the allocated memory (this is used to avoid locking the queue unnecessarily)
                synchronized(this.ack_queue) {
                    while(this.ack_queue.remainingCapacity() == 0 && this.alive.get()) { //while we don't have space in the allocated memory
                        System.out.println("ACK SCHEDULER WAITS. ID:" + Thread.currentThread().getId());
                        this.ack_queue.wait(200); //wait 200ms or until notified by sender
                    }
                }
            }
            this.ack_queue.add(batch); //add it to the schedule                     
        } catch (Exception e) {
            System.out.println("ACK SCHEDULER INTERRUPTED. ID:" + Thread.currentThread().getId());
        }
    }
	
	public void stop_() {
        this.alive.set(false); //stops us from sending further messages
	}
}
