package cs451.message;

import java.nio.ByteBuffer;
import java.util.Arrays;

import cs451.Constants;
import cs451.message.MessageLocal;

/*
 * Message class that includes all the possible fields.
 * During sending, used from BEB to UDP level; during delivering,
 * used from Network to SL level.
 */
public class Message {

    private byte source;
    private byte dest;
    private int seq;
    private byte[] m;
    
    public Message(MessageLocal ml, byte dest, int seq) {
        this.source = ml.getSource();
        this.dest = dest;
        this.seq = seq;
        this.m = ml.getM();
    }

    public Message(byte[] data, int index) {
        this.source = data[index+0];
        this.dest = data[index+1];
        this.seq = ByteBuffer.wrap(data).getInt(index+2);
        this.m = Arrays.copyOfRange(data, index + 6, index + 6 + Constants.LATTICE_PAYLOAD_SIZE);
    }

    public byte[] bytes() {
        return ByteBuffer.allocate(Constants.UDP_MESSAGE_SIZE).put(this.source).put(this.dest).putInt(this.seq).put(this.m).array();
    }

    public byte getSource() {
        return this.source;
    }

    public int getSourceInt() {
        if (this.source == -128) return 128; //for the edge case return 128
        return (int) this.source; //otherwise return the id directly
    }

    public byte getDest() {
        return this.dest;
    }

    public int getDestInt() {
        if (this.dest == -128) return 128; //for the edge case return 128
        return (int) this.dest; //otherwise return the id directly
    }

    public int getSeq() {
        return this.seq;
    }

    public byte[] getM() {
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

        return this.getSeq() == m.getSeq();
    }

    @Override
    public int hashCode() {
        return this.getSeq();
    }

}
