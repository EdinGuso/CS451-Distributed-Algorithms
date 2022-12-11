package cs451.message;

import java.nio.ByteBuffer;
import java.util.LinkedList;

import cs451.Constants;
import cs451.message.Message;

/*
 * Allows the user to batch messages before placing them on the network.
 * Important for better utilization of the network bandwidth.
 */
public class MessageBatch {

    private LinkedList<Message> batch;
    
    public MessageBatch() {
        this.batch = new LinkedList<Message>();
    }

    public MessageBatch(byte[] data) {
        this.batch = new LinkedList<Message>();
        int num_m = ByteBuffer.wrap(data).getInt(0);
        for (int i = 0; i < num_m; i++) {
            this.batch.add(new Message(data, i * Constants.UDP_MESSAGE_SIZE + 4));
        }
    }

    public void add(Message m) {
        this.batch.add(m);
    }

    public boolean isFull () {
        return this.batch.size() >= Constants.UDP_MESSAGE_LIMIT;
    }

    public boolean isEmpty() {
        return this.batch.size() == 0;
    }

    public void clear() {
        this.batch.clear();
    }

    public LinkedList<Message> messages() {
        return this.batch;
    }

    public int size() {
        return this.batch.size();
    }

    public byte getSource() {
        return this.batch.get(0).getSource();
    }

    public byte getDest() {
        return this.batch.get(0).getDest();
    }

    public byte[] bytes() {
        // CONSIDER FIXING THIS!!!!
        // !!!!
        // !!!!
        
        byte[] data = new byte[Constants.UDP_PACKET_SIZE];
        byte[] num_m_byte = ByteBuffer.allocate(4).putInt(this.batch.size()).array(); //turn the number of messages into a byte array
        byte[] m_byte = new byte[Constants.UDP_MESSAGE_SIZE];
        System.arraycopy(num_m_byte, 0, data, 0, num_m_byte.length); //add it to the data
        for (int i = 0; i < this.batch.size(); i++) { //for each message in the batch
            m_byte = this.batch.get(i).bytes(); //get the byte form of message
            System.arraycopy(m_byte, 0, data, i * Constants.UDP_MESSAGE_SIZE + 4, Constants.UDP_MESSAGE_SIZE); //add it to the data
        }
        return data;
    }
}
