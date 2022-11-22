package cs451.message;

import cs451.message.MessageLocal;

/*
 * Message class that does not include source or dest fields.
 * During sending, used at URB level; during delivering,
 * used from URB to APP level. Also used in ack hashmap
 * at URB level as the key.
 */
public class MessageOrigin {

    private byte origin;
    private int seq;
    
    public MessageOrigin(MessageLocal ml) {
        this.origin = ml.getOrigin();
        this.seq = ml.getSeq();
    }

    public MessageOrigin(byte origin, int seq) {
        this.origin = origin;
        this.seq = seq;
    }

    public byte getOrigin() {
        return this.origin;
	}

    public int getOriginInt() {
        if (this.origin == -128) return 128; //for the edge case return 128
        return (int) this.origin; //otherwise return the id directly
	}

    public int getSeq() {
        return this.seq;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        
        if (o == null)
            return false;
        
        if (this.getClass() != o.getClass())
            return false;

        MessageOrigin mo = (MessageOrigin) o;

        return this.getOrigin() == mo.getOrigin() && this.getSeq() == mo.getSeq();
    }

    @Override
    public int hashCode() {
        //origin is in range of 1 - 128
        return this.getOriginInt() + (128 * this.getSeq());
    }
}
