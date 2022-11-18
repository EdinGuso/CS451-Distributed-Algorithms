package cs451.message;

import java.util.List;
import java.util.HashMap;

import cs451.Host;
import cs451.Constants;
import cs451.util.DeliveredZip;

public class MessageIdentifier {

    private byte id;
    private byte origin;
    
    public MessageIdentifier(int id, int origin) {
        this.id = (byte) id;
        this.origin = (byte) origin;
    }

    public int getId() {
        if (this.id == -128) return 128; //for the edge case return 128
        return (int) this.id; //otherwise return the id directly
	}

    public int getOrigin() {
        if (this.origin == -128) return 128; //for the edge case return 128
        return (int) this.origin; //otherwise return the id directly
	}
    
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        
        if (o == null)
            return false;
        
        if (this.getClass() != o.getClass())
            return false;

        MessageIdentifier i = (MessageIdentifier) o;

        return this.getId() == i.getId() && this.getOrigin() == i.getOrigin();
    }

    @Override
    public int hashCode() {
        //id and origin are in range of 1 - 128
        return (this.getId() - 1) + (128 * (this.getOrigin() - 1));
    }

}
