package cs451.links;

import java.net.DatagramSocket;
import java.util.List;

import cs451.Host;
import cs451.udp.UDPServer;
import cs451.udp.UDPClient;
import cs451.message.Message;
import cs451.message.MessageBatch;
import cs451.links.StubbornLinks;

/*
 * Implements the fairlosslinks part of the protocol.
 * Constructs sender and recevier threads, and provides
 * functions to upper layer (sl) for starting, sending and stopping;
 * as well as a deliver function to lower layer (deliverer).
 */
public class FairLossLinks {

    private StubbornLinks upper_layer;
    private UDPServer receiver;
    private UDPClient sender;
    private DatagramSocket socket;
    
    public FairLossLinks(int port, List<Host> hosts, StubbornLinks sl) {
        try {
            this.socket = new DatagramSocket(port);
            this.upper_layer = sl;
            this.receiver = new UDPServer(this.socket, this);
            this.sender = new UDPClient(this.socket, hosts);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(Message m) {
        this.sender.scheduleMessage(m);
    }

    public void sendAck(MessageBatch batch) {
        this.sender.scheduleAck(batch);
    }

    public void start() {
        this.receiver.start();
        this.sender.start();
    }

    public void stop_() {
        this.sender.stop_();
        this.sender.interrupt();
        try { this.sender.join(); } catch (Exception e) { System.out.println("Main got interrupted while waiting for sender to join."); } //should not happen
        this.receiver.stop_();
        //this.receiver.interrupt(); don't send interrupt. it will stop by getting its socket closed
        try { this.receiver.join(); } catch (Exception e) { System.out.println("Main got interrupted while waiting for receiver to join."); } //should not happen
    }

    public void deliver (MessageBatch batch) {
        this.upper_layer.deliver(batch);
    }
}
