package cs451.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

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
    private DatagramSocket socket;
    private DatagramPacket packet;
    private byte[] buffer;

    public UDPServer(DatagramSocket socket, FairLossLinks fll) {
        try {
            this.upper_layer = fll;
            this.deliverer = new Deliverer(fll);
            this.socket = socket;
            this.buffer = new byte[Constants.UDP_PACKET_SIZE];
		}
        catch (Exception e) {
			e.printStackTrace();
		}
    }

    public void run() {
        this.deliverer.start();
        try {
            while(true) {
                this.packet = new DatagramPacket(this.buffer, this.buffer.length);
                this.socket.receive(this.packet);
                if (this.buffer[0] == 1) {
                    Constants.FLOW_CONTROL.processFlowInfo(Arrays.copyOf(this.buffer, this.buffer.length));
                }
                else {
                    this.deliverer.scheduleDelivery(Arrays.copyOf(this.buffer, this.buffer.length));
                }
            }
        }
        catch (Exception e) {
            System.out.println("Server thread forced to stop by closing socket.");
        } finally {
            System.out.println("Server thread stopped. ID:" + Thread.currentThread().getId());
        }
    }

	public void stop_() {
        this.deliverer.stop_();
        try { this.deliverer.join(); } catch (Exception e) { e.printStackTrace(); }
        this.socket.close();
	}
}
