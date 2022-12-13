package cs451.message;

import cs451.message.Message;

/*
 * Message class that does not include dest field.
 * During sending, used from APP to BEB level; during delivering,
 * used from SL to APP level.
 */
public class MessageLocal {

    private byte source;
    private byte[] m;

    public MessageLocal(byte source, byte[] m) {
        this.source = source;
        this.m = m;
    }
    
    public MessageLocal(Message m) {
        this.source = m.getSource();
        this.m = m.getM();
    }
	
    public byte getSource() {
        return this.source;
	}

    public int getSourceInt() {
        if (this.source == -128) return 128; //for the edge case return 128
        return (int) this.source; //otherwise return the id directly
	}

    public byte[] getM() {
        return this.m;
    }
}
