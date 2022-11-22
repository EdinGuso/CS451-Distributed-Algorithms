package cs451.message;

import java.nio.ByteBuffer;

import cs451.Constants;
import cs451.message.MessageLocal;

/*
 * Message class that includes all the possible fields.
 * During sending, used from BEB to UDP level; during delivering,
 * used from Network to SL level.
 */
public class Message {

    private byte origin;
    private byte source;
    private byte dest;
    private int seq;
    
    public Message(MessageLocal ml, byte dest) {
        this.origin = ml.getOrigin();
        this.source = ml.getSource();
        this.dest = dest;
        this.seq = ml.getSeq();
    }

    public Message(byte origin, byte source, byte dest, int seq) {
        this.origin = origin;
        this.source = source;
        this.dest = dest;
        this.seq = seq;
    }

    public Message(byte[] data) {
        this.origin = data[0];
        this.source = data[1];
        this.dest = data[2];
        this.seq = ByteBuffer.wrap(data).getInt(3);
    }

    public Message(byte[] data, int index) {
        this.origin = data[index];
        this.source = data[index+1];
        this.dest = data[index+2];
        this.seq = ByteBuffer.wrap(data).getInt(index+3);
    }

    public byte[] bytes() {
        return ByteBuffer.allocate(Constants.UDP_MESSAGE_SIZE).put(this.origin).put(this.source).put(this.dest).putInt(this.seq).array();
    }

    public byte getOrigin() {
        return this.origin;
    }

    public int getOriginInt() {
        if (this.origin == -128) return 128; //for the edge case return 128
        return (int) this.origin; //otherwise return the id directly
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
}
