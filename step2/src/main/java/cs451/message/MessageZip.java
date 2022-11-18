package cs451.message;

import java.nio.ByteBuffer;
import java.util.Arrays;

import cs451.Constants;
import cs451.message.Message;

public class MessageZip {

    private byte id;
    private byte origin;
    private int m;
    
    public MessageZip(byte[] data) {
        this.id = ByteBuffer.wrap(Arrays.copyOfRange(data, 0, 1)).get();
        this.origin = ByteBuffer.wrap(Arrays.copyOfRange(data, 1, 2)).get();
        this.m = ByteBuffer.wrap(Arrays.copyOfRange(data, 2, 6)).getInt();
    }

    public MessageZip(Message m) {
        this.id = (byte) m.getId();
        this.origin = (byte) m.getOrigin();
        this.m = m.getM();
    }

    public MessageZip(int id, int origin, int m) {
        this.id = (byte) id;
        this.origin = (byte) origin;
        this.m = m;
    }

    public byte[] bytes() { //pack the id, origin and message into a byte array
        return ByteBuffer.allocate(Constants.UDP_MESSAGE_SIZE).put(this.id).put(this.origin).putInt(this.m).array();
    }
	
	public int getId() {
        if (this.id == -128) return 128; //for the edge case return 128
        return (int) this.id; //otherwise return the id directly
	}

    public int getOrigin() {
        if (this.origin == -128) return 128; //for the edge case return 128
        return (int) this.origin; //otherwise return the id directly
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

        return this.getId() == m.getId() && this.getOrigin() == m.getOrigin() && this.getM() == m.getM();
    }

    @Override
    public int hashCode() {
        //id and origin are in range of 1 - 128
        //hashcode loops around every 2^17 = 131,072 messages => there should not be collisions
        return (this.getId() - 1) + (128 * (this.getOrigin() - 1)) + (16384 * this.m);
    }

}
