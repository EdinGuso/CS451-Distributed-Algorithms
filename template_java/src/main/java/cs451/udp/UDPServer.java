package cs451.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import cs451.Constants;
import cs451.util.Deliverer;
import cs451.links.FairLossLinks;

/*
 * Implements the receiver part of the protocol.
 * Waits for packets on the socket and when packets
 * arrive schedules them for delivery for another thread.
 */
public class UDPServer extends Thread {

    private FairLossLinks upper_layer;
    private Deliverer deliverer;
    public ArrayBlockingQueue<byte[]> queue; //made public so that Deliverer can use
    private DatagramSocket socket;
    private DatagramPacket packet;
    private AtomicBoolean alive;
    private byte[] buffer;

    public UDPServer(DatagramSocket socket, FairLossLinks link) {
        try {
            this.upper_layer = link;
            this.deliverer = new Deliverer(this.upper_layer, this);
            this.queue = new ArrayBlockingQueue(Constants.UDP_DELIVERER_QUEUE_SIZE); //limits the max size of the delivery queue
            this.socket = socket;
            this.alive = new AtomicBoolean(true);
            this.buffer = new byte[Constants.UDP_PACKET_SIZE]; //max size of the UDP packet
		}
        catch (Exception e) {
			e.printStackTrace();
		}
    }

    public void run() {
        this.deliverer.start(); //start a single thread for delivery task
        try {
            while(this.alive.get()) {
                this.packet = new DatagramPacket(this.buffer, this.buffer.length);
                this.socket.receive(this.packet); //wait for the packet
                this.schedule_delivery(Arrays.copyOf(this.buffer, this.buffer.length));
            }
        }
        catch (Exception e) {
            System.out.println("SERVER INTERRUPTED (socket closed). ID:" + Thread.currentThread().getId());
        }
    }

    public void schedule_delivery(byte[] data) {
        try {
            if (this.queue.remainingCapacity() == 0 && this.alive.get()) { //if we don't have space in the allocated memory (this is used to avoid locking the queue unnecessarily)
                synchronized(this.queue) {
                    while(this.queue.remainingCapacity() == 0 && this.alive.get()) { //while we don't have space in the allocated memory
                        System.out.println("SERVER WAITS.");
                        this.queue.wait(100); //wait 100ms or until notified by deliverer
                    }
                }
            }
            this.queue.add(data); //add it to the schedule
            if (this.queue.size() == Constants.UDP_DELIVERER_QUEUE_SIZE / 16) { //if we have put some objects
                synchronized(this.queue) {
                    this.queue.notifyAll(); //notify the deliverers to wake up
                }
            }
        } catch (Exception e) {
            System.out.println("SERVER INTERRUPTED. ID:" + Thread.currentThread().getId());
        }
    }

	public void stop_() {
        this.deliverer.stop_();
        this.deliverer.interrupt(); //in case deliverer was stuck in wait
        try { this.deliverer.join(); } catch (Exception e) { System.out.println("Server got interrupted while waiting for deliverer to join."); } //should not happen
        this.alive.set(false); //stops us from scheduling more message batches
        this.socket.close();
        System.out.println("Socket closed.");
        this.queue.clear();
        System.out.println("Deliverer queue cleared.");
	}
}
