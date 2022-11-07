package cs451.util;

import java.util.LinkedList;
import java.util.Arrays;
import java.nio.ByteBuffer;

import cs451.Constants;
import cs451.util.MessageZip;

public class MessageZipBatch {

    private LinkedList<MessageZip> batch;
    
    public MessageZipBatch(byte[] data) {
        try {
            assert data.length == Constants.UDP_PACKET_SIZE; //ensure the given byte array is of allowed size
            this.batch = new LinkedList<MessageZip>();
            int num_m = ByteBuffer.wrap(Arrays.copyOfRange(data, 0, 4)).getInt(); //number of messages in the batch
            for (int i = 0; i < num_m; i++) {
                this.batch.add(new MessageZip(Arrays.copyOfRange(data, i * Constants.UDP_MESSAGE_SIZE + 4, (i + 1) * Constants.UDP_MESSAGE_SIZE + 4)));
            }
		}
        catch (Exception e) {
			e.printStackTrace();
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
