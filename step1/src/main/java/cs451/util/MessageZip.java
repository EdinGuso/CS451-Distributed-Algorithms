package cs451.util;

import java.nio.ByteBuffer;
import java.util.Arrays;

import cs451.Constants;
import cs451.util.Message;

public class MessageZip {

    private byte id;
    private int m;
    
    public MessageZip(byte[] data) {
        this.id = ByteBuffer.wrap(Arrays.copyOfRange(data, 0, 1)).get();
        this.m = ByteBuffer.wrap(Arrays.copyOfRange(data, 1, 5)).getInt();
    }

    public MessageZip(Message m) {
        this.id = (byte) m.getId();
        this.m = m.getM();
    }

    public byte[] bytes() {
        return ByteBuffer.allocate(Constants.UDP_MESSAGE_SIZE).put(this.id).putInt(this.m).array(); //pack the id and message into a byte array
    }
	
	public int getId() {
        if (this.id == -128) return 128; //for the edge case return 128
        return (int) this.id; //otherwise return the id directly
	}

    public int getM() {
        return this.m;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        
        if (o == null)
            return false;
        
        if (this.getClass() != o.getClass())
            return false;

        MessageZip m = (MessageZip) o;

        return this.getId() == m.getId() && this.m == m.getM();
    }

    @Override
    public int hashCode() {
        //id is in range of 1 - 128
        return (this.getId() - 1) + (128 * this.m);
    }

}
