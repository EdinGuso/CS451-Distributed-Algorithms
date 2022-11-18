package cs451.message;

public class MessageInitial {

    private byte origin;
    private int m;
    
    public MessageInitial(int origin, int m) {
        this.origin = (byte) origin;
        this.m = m;
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

        MessageInitial m = (MessageInitial) o;

        return this.getOrigin() == m.getOrigin() && this.getM() == m.getM();
    }

    @Override
    public int hashCode() {
        //origin is in range of 1 - 128
        return (this.getOrigin() - 1) + (128 * this.m);
    }
}
