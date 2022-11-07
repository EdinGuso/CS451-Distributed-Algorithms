package cs451.app;

import java.util.List;
import java.util.LinkedList;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;

import cs451.Host;
import cs451.Constants;
import cs451.util.Message;
import cs451.util.MessageZip;
import cs451.links.PerfectLinks;

/*
 * Implements the wrapper around perfect links that runs everything.
 * Constructs the lower layer and list for storing delivered/sent messages,
 * occasionally logging them to the file and provides functions to
 * upper layer (main) for starting, sending and stopping; as well as
 * a deliver function to lower layer.
 */
public class Application {

    private PerfectLinks lower_layer;
    private LinkedList<MessageZip> incoming;
    private LinkedList<MessageZip> outgoing;
    private List<Host> hosts;
    private Host target;
    private String output_filename;
    private AtomicBoolean alive;
    private int num_messages;
    private int capacity;
    private int id;

    public Application(List<Host> hosts, int id, int port, Host target, int num_messages, String output_filename) {
        this.lower_layer = new PerfectLinks(hosts, id, port, target, this);
        this.incoming = new LinkedList<MessageZip>();
        this.outgoing = new LinkedList<MessageZip>();
        this.hosts = hosts;
        this.target = target;
        this.output_filename = output_filename;
        this.alive = new AtomicBoolean(true);
        this.num_messages = num_messages;
        this.capacity = Constants.APP_QUEUE_SIZE;
        this.id = id;
    }

    public void send(Message m) {
        if (this.outgoing.size() >= this.capacity) { //if we don't have space in the data structure
            this.write(); //write the accumulated messages to file
        }
        this.outgoing.add(new MessageZip(m)); //add a new item to the list to be written
        this.lower_layer.send(m); //send the message to lower layers
    }

    public void start() {
        this.lower_layer.start(); //start the underlying protocols
        if (this.id == this.target.getId()) { //if we are the receiving process
            return; //don't send any messages
        }
        for (int i = 1; i <= this.num_messages && this.alive.get(); i++) { //otherwise send the messages
            this.send(new Message(this.target.getIp(), this.target.getPort(), this.id, i));
        }
    }

    public void stop_() {
        if (this.alive.compareAndSet(true, false)) { //if we haven't stopped before stop
            this.lower_layer.stop_(); //stop the lower layers
            this.write(); //write all the messages that were not written before
        }
    }

    public void deliver(MessageZip m) {
        if (this.incoming.size() >= this.capacity) { //if we don't have space in the data structure
            this.write(); //write the accumulated messages to file
        }
        this.incoming.add(m); //add another message to be written later
    }

    public void write() {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(this.output_filename, true)); //open the file
            MessageZip m;
            if (this.id == this.target.getId()) { //if I am the receiver process
                while (!this.incoming.isEmpty()) { //and there are more messages to be written
                    m = this.incoming.poll(); //remove the message
                    writer.println("d " + m.getId() + " " + m.getM()); //write it to the file as delivered
                }
            }
            else { //if I am the sending process
                while (!this.outgoing.isEmpty()) { //and there are more messages to be written
                    m = this.outgoing.poll(); //and there are more messages to be written
                    writer.println("b " + m.getM()); //write it to the file as sent
                }
            }
            writer.close(); //close the writer
        } catch (Exception e) { e.printStackTrace(); }
    }
}
