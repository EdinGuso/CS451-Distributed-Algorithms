package cs451.util;

import java.nio.ByteBuffer;
import java.util.Arrays;

import cs451.Constants;

public class Message {

    private String ip;
    private int port;
    private byte id;
    private int m;
    
    public Message(String ip, int port, int id, int m) {
        try {
            assert ip.length() <= 15; //ensure that our string does not use more memory than allowed
            this.ip = ip;
            this.port = port;
            this.id = (byte) id; //this.id becomes -128 if id is 128 => handled in getInt()
            this.m = m;
		}
        catch (Exception e) {
			e.printStackTrace();
		}
    }

    public Message(byte[] data) {
        try {
            assert data.length == Constants.UDP_MESSAGE_SIZE; //ensure the given byte array is of allowed size
            this.ip = ""; //we don't know the ip by looking at the received data
            this.port = 0; //we don't know the port by looking at the received data
            this.id = ByteBuffer.wrap(Arrays.copyOfRange(data, 0, 1)).get();
            this.m = ByteBuffer.wrap(Arrays.copyOfRange(data, 1, 5)).getInt();
		}
        catch (Exception e) {
			e.printStackTrace();
		}
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

    public String getIp() {
        return this.ip;
    }

    public int getPort() {
        return this.port;
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

        return this.getId() == m.getId() && this.m == m.getM();
    }

    @Override
    public int hashCode() {
        //id is in range of 1 - 128
        return (this.getId() - 1) + (128 * this.m);
    }

}
