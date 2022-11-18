package cs451.util;

import java.util.TreeSet;

import cs451.Constants;

public class DeliveredZip {

    private TreeSet<Integer> out_of_order;
    private int ordered_max;
    
    public DeliveredZip() {
        this.out_of_order = new TreeSet<Integer>();
        this.ordered_max = 0;
    }

    public void add(int m) {
        if (this.contains(m)) { //if we have already delivered this message
            return; //do nothing
        }
        if (this.isOrdered(m)) { //if the message is next in order
            this.ordered_max++; //increase the ordered set
            while (!this.out_of_order.isEmpty() && this.isOrdered(this.out_of_order.first())) { //and see if waiting messages are ordered now
                this.out_of_order.pollFirst();
                this.ordered_max++;
            }
            return;
        }
        this.out_of_order.add(m); //if we end up here the message is a new out of order message
    }

    public boolean contains(int m) {
        if (m <= this.ordered_max) { //if the message is in the ordered set
            return true; //then it has already been delivered
        }
        if (this.out_of_order.contains(m)) { //or if it is in the out of order set
            return true; //then it has also already been delivered
        }
        return false;
    }

    public boolean isOrdered(int m) {
        return m == this.ordered_max + 1;
    }

    public int size() {
        return this.out_of_order.size();
    }
}
