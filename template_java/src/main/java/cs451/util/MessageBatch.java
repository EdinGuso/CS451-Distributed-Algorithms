package cs451.util;

import java.util.LinkedList;
import java.nio.ByteBuffer;

import cs451.Constants;
import cs451.util.Message;
import cs451.util.MessageZip;
import cs451.util.MessageZipBatch;

public class MessageBatch {

    private LinkedList<MessageZip> batch;
    private String ip;
    private int port;
    
    public MessageBatch() {
        this.batch = new LinkedList<MessageZip>();
        this.ip = "";
        this.port = 0;
    }

    public MessageBatch(MessageZipBatch batch, String ip, int port) {
        this.batch = batch.msgs();
        this.ip = ip;
        this.port = port;
    }

    public void add(Message m) {
        if (this.batch.size() == 0) { //if this is the first message of the batch
            this.ip = m.getIp(); //decide this batch's destination as the first message inserted
            this.port = m.getPort();
        }
        if (!this.isFull()) {
            this.batch.add(new MessageZip(m));
        }
    }

    public boolean isFull () {
        return this.batch.size() >= Constants.UDP_MESSAGE_LIMIT;
    }

    public void clear() {
        this.batch.clear();
    }

    public LinkedList<MessageZip> msgs() {
        return this.batch;
    }

    public int size() {
        return this.batch.size();
    }

    public String getIp() {
        return this.ip;
    }

    public int getPort() {
        return this.port;
    }

    public byte[] bytes() {
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
