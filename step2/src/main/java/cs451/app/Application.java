package cs451.app;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import cs451.Host;
import cs451.Constants;
import cs451.message.MessageZip;
import cs451.message.MessageInitial;
import cs451.broadcast.FIFOBroadcast;

/* TO EDITTT
 * Implements the wrapper around perfect links that runs everything.
 * Constructs the lower layer and list for storing delivered/sent messages,
 * occasionally logging them to the file and provides functions to
 * upper layer (main) for starting, sending and stopping; as well as
 * a deliver function to lower layer.
 */
public class Application {

    private FIFOBroadcast lower_layer;
    private ArrayBlockingQueue<String> logs;
    private String output_filename;
    private int num_messages;
    private int id;
    private AtomicBoolean alive;

    public Application(Host host, List<Host> hosts, int num_messages, String output_filename) {
        this.lower_layer = new FIFOBroadcast(host, hosts, this);
        this.logs = new ArrayBlockingQueue<String>(Constants.APP_QUEUE_SIZE);
        this.output_filename = output_filename;
        this.num_messages = num_messages;
        this.id = host.getId();
        this.alive = new AtomicBoolean(true);
    }

    public void broadcast(MessageZip m) {
        //System.out.println("Broadcasted from APP: (" + m.getOrigin() + "," + m.getM() + ")");
        if (this.logs.remainingCapacity() < 2) { //if we don't have space in the data structure
            this.write(); //write the accumulated messages to file
        }
        this.logs.add("b " + Integer.toString(m.getM())); //add a new item to the list to be written
        this.lower_layer.broadcast(m); //send the message to lower layers
        
    }

    public void start() {
        this.lower_layer.start(); //start the underlying protocols
        for (int i = 1; i <= this.num_messages && this.alive.get(); i++) {
            this.broadcast(new MessageZip(this.id, this.id, i));
        }
    }

    public void stop_() {
        this.alive.set(false);
        this.lower_layer.stop_(); //stop the lower layers
        this.write(); //write all the messages that were not written before

    }

    public void deliver(MessageInitial m) {
        //System.out.println("Delivered to APP: (" + m.getOrigin() + "," + m.getM() + ")");
        if (this.logs.remainingCapacity() < 2) { //if we don't have space in the data structure
            if (this.logs.remainingCapacity() < 2) {
                this.write(); //write the accumulated messages to file
            }
        }
        this.logs.add("d " + Integer.toString(m.getOrigin()) + " " + Integer.toString(m.getM())); //add another message to be written later
    }

    public void write() {
        try {
            synchronized (this.logs) {
                PrintWriter writer = new PrintWriter(new FileWriter(this.output_filename, true)); //open the file
                while (!this.logs.isEmpty()) { //and there are more messages to be written
                    writer.println(this.logs.poll()); //write it
                }
                writer.close(); //close the writer
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
