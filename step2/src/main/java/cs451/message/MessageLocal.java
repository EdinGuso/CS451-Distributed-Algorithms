package cs451.message;

import cs451.message.Message;

/*
 * Message class that does not include dest field.
 * During sending, used from URB to BEB level; during delivering,
 * used from SL to URB level.
 */
public class MessageLocal {

    private byte origin;
    private byte source;
    private int seq;

    public MessageLocal(byte origin, byte source, int seq) {
        this.origin = origin;
        this.source = source;
        this.seq = seq;
    }
    
    public MessageLocal(Message m) {
        this.origin = m.getOrigin();
        this.source = m.getSource();
        this.seq = m.getSeq();
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

    public int getSeq() {
        return this.seq;
    }
}
