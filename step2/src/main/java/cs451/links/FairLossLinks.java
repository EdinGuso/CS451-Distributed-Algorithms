package cs451.links;

import java.net.DatagramSocket;
import java.util.HashMap;

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
    
    public FairLossLinks(HashMap<Byte, Host> hosts_map, Host self, StubbornLinks sl) {
        try {
            this.upper_layer = sl;
            this.socket = new DatagramSocket(self.getPort());
            this.receiver = new UDPServer(this.socket, this);
            this.sender = new UDPClient(hosts_map, this.socket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Forwards the send signal to client.
     */
    public void send(Message m) {
        this.sender.scheduleMessage(m);
    }

    /*
     * Forwards the send signal to client. (Ack)
     */
    public void sendAck(MessageBatch batch) {
        this.sender.scheduleAck(batch);
    }

    /*
     * Forwards the start signal to server and client.
     */
    public void start() {
        this.receiver.start();
        this.sender.start();
    }

    /*
     * Forwards the stop signal to server and client.
     */
    public void stop_() {
        this.sender.stop_();
        try { this.sender.join(); } catch (Exception e) { e.printStackTrace(); }
        this.receiver.stop_();
        try { this.receiver.join(); } catch (Exception e) { e.printStackTrace(); }
    }

    /*
     * Forwards the deliver signal to upper layer.
     */
    public void deliver (MessageBatch batch) {
        this.upper_layer.deliver(batch);
    }
}
