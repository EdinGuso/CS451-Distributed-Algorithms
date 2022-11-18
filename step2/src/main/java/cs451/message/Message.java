package cs451.message;

import java.nio.ByteBuffer;
import java.util.Arrays;

import cs451.Constants;

public class Message {

    private String ip;
    private int port;
    private byte id;
    private byte origin;
    private int m;
    
    public Message(String ip, int port, int id, int origin, int m) {
        try {
            assert ip.length() <= 15; //ensure that our string does not use more memory than allowed
            this.ip = ip;
            this.port = port;
            this.id = (byte) id; //this.id becomes -128 if id is 128 => handled in getId()
            this.origin = (byte) origin; //this.origin becomes -128 if id is 128 => handled in getOrigin()
            this.m = m;
		}
        catch (Exception e) {
			e.printStackTrace();
		}
    }

    public byte[] bytes() { //pack the id, origin and message into a byte array
        return ByteBuffer.allocate(Constants.UDP_MESSAGE_SIZE).put(this.id).put(this.origin).putInt(this.m).array();
    }

    public String getIp() {
        return this.ip;
    }

    public int getPort() {
        return this.port;
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

        Message m = (Message) o;

        return this.getIp().equals(m.getIp()) && this.getPort() == m.getPort() && this.getId() == m.getId() && this.getOrigin() == m.getOrigin() && this.getM() == m.getM();
    }

    @Override
    public int hashCode() {
        //id and origin are in range of 1 - 128
        //hashcode loops around every 2^17 = 131,072 messages => there should not be collisions
        return (this.getId() - 1) + (128 * (this.getOrigin() - 1)) + (16384 * this.m);
    }

}
