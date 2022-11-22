package cs451.app;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import cs451.Host;
import cs451.Constants;
import cs451.message.MessageOrigin;
import cs451.broadcast.FIFOBroadcast;

/*
 * Implements the wrapper around FIFO Broadcast that runs everything.
 * Constructs the lower layer and a queue for storing delivered/broadcaster
 * messages, occasionally logging them to the file and provides functions to
 * upper layer (main) for starting and stopping; as well as
 * a deliver function to lower layer.
 */
public class Application {

    private FIFOBroadcast lower_layer;
    private ArrayBlockingQueue<String> logs;
    private PrintWriter writer;
    private String output_filename;
    private int num_messages;
    private AtomicBoolean alive;

    public Application(HashMap<Byte, Host> hosts_map, Host self, int num_messages, String output_filename) {
        this.lower_layer = new FIFOBroadcast(hosts_map, self, this);
        this.logs = new ArrayBlockingQueue<String>(Constants.APP_QUEUE_SIZE);
        this.output_filename = output_filename;
        this.num_messages = num_messages;
        this.alive = new AtomicBoolean(true);
    }

    /*
     * Forwards the broadcast signal to lower layer
     * and writes log when necessary.
     */
    public void broadcast(int seq) {
        while (!this.logs.offer("b " + Integer.toString(seq))) { //while logs are full and we cannot append new strings
            this.write(); //write logs to the file
        }
        this.lower_layer.broadcast(seq);
    }

    /*
     * Forwards the start signal to lower layer. Keeps sending
     * broadcast signals untill all are sent or SIGTERM is received.
     */
    public void start() {
        this.lower_layer.start(); //start the underlying protocols
        for (int i = 1; i <= this.num_messages && this.alive.get(); i++) {
            this.broadcast(i);
        }
    }

    /*
     * Prevents future broadcasts. Forwards the stop signal
     * to lower layer. Writes any remaining items in logs.
     */
    public void stop_() {
        this.alive.set(false);
        this.lower_layer.stop_();
        this.write();
    }

    /*
     * After receiving deliver signal from lower layer,
     * to lower layer. Writes any remaining items in logs.
     */
    public void deliver(MessageOrigin mo) {
        while (!this.logs.offer("d " + Integer.toString(mo.getOriginInt()) + " " + Integer.toString(mo.getSeq()))) {
            this.write();
        }
    }

    /*
     * Writes the contents of logs into the output file.
     */
    public void write() {
        try {
            synchronized (this.logs) { //synchronize since main and deliverer may access concurrently
                this.writer = new PrintWriter(new FileWriter(this.output_filename, true)); //open the file
                while (!this.logs.isEmpty()) { //if there are more messages to be written
                    this.writer.println(this.logs.poll()); //write it
                }
                this.writer.close(); //close the file
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
