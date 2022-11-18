package cs451.udp;

import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import cs451.Host;
import cs451.Constants;
import cs451.message.Message;
import cs451.message.MessageBatch;
import cs451.message.MessageBatchMap;

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
    private MessageBatchMap batch_map;
    private MessageBatch ack_batch;
    private AtomicBoolean alive;
    private byte[] buffer;
    private int count;
    
    public UDPClient(DatagramSocket socket, List<Host> hosts) {
        try {
			this.socket = socket;
            this.queue = new ArrayBlockingQueue(Constants.UDP_SENDER_QUEUE_SIZE); //limits the max size of the delivery queue
            this.ack_queue = new ArrayBlockingQueue(Constants.UDP_ACK_QUEUE_SIZE); //limits the max size of the delivery queue
            this.batch_map = new MessageBatchMap(hosts);
            this.alive = new AtomicBoolean(true);
            this.buffer = new byte[Constants.UDP_PACKET_SIZE];
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
                while ((this.queue.peek() != null || this.batch_map.anyEarlySend()) && this.ack_queue.peek() == null && this.alive.get()) { //if the queue is not empty or if we have promised an early send
                    while (this.queue.peek() != null && !this.batch_map.anyBatchIsFull() && this.alive.get()) { //if the queue is not empty and we still have space in any batch
                        this.batch_map.add(this.queue.poll()); //then add the first message in the queue to the appropriate batch
                        if (this.queue.remainingCapacity() == Constants.UDP_SENDER_QUEUE_SIZE / 8) { //if we have used some objects
                            synchronized(this.queue) {
                                this.queue.notifyAll(); //notify the send schedulers to wake up
                            }
                        }
                    }
                    if ((this.batch_map.anyBatchIsFull() || (this.batch_map.anyEarlySend() && this.queue.peek() == null)) && this.alive.get()) { //if any batch is full or (we have promised an early send and there are no messages waiting in the queue)
                        for (MessageBatch batch : this.batch_map.eligibleBatches()) { //loop over every eligible batch (either full or promised early start)
                            this.buffer = batch.bytes();
                            this.packet = new DatagramPacket(this.buffer, this.buffer.length, InetAddress.getByName(batch.getIp()), batch.getPort()); //generate the datagram packet
                            this.socket.send(this.packet); //send the datagram packet
                            this.count = this.count + batch.size();
                            batch.clear(); //clear the batch
                        }
                        this.batch_map.resetEarlySend(); //reset the early send flag
                    }
                }
                if (!this.batch_map.allBatchesAreEmpty() && this.alive.get()) { //if not all batches are empty
                    this.batch_map.setEarlySend(); //promise an early send to them
                }

                // IF BOTH ARE EMPTY, SLEEP
                // We don't use signals because signals might come from 2 sources!
                if (this.queue.size() == 0 && this.ack_queue.size() == 0 && !this.batch_map.anyEarlySend() && this.alive.get()) { //if there is nothing to send, and we have not promised an early send
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
                        //System.out.println("CLIENT BLOCKED FOR: ID-" + Thread.currentThread().getId());
                        this.queue.wait(200); //wait 200ms or until notified by sender
                    }
                }
            }
            if (this.queue.remainingCapacity() < 3) {  //if we have less than 2 space left we need to synchronize it
                synchronized(this.queue) { //since there are 2 threads (main, scheduler, broadcaster) that will be accessing this
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
                        System.out.println("CLIENT (ACK) BLOCKED FOR: ID-" + Thread.currentThread().getId());
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
