package cs451.util.structures;

/*
 * Data structure for storing flow rates of processes.
 */
public class ArrayQueue {

    private int[] list;
    private int index;
    private int count;
    private int max;
    
    public ArrayQueue(int size) {
        this.list = new int[size];
        this.index = 0;
    }

    public void add(int val) {
        this.list[this.index] = val;
        this.index = (this.index + 1) % this.list.length;
    }

    public int getMax() {
        this.count = 0;
        this.max = 0;
        for (int val : this.list) {
            if (val == this.max) {
                this.count++;
            }
            else if (val > this.max) {
                this.max = val;
            }
        }
        if (this.count >= this.list.length - 1) {
            return Integer.MAX_VALUE; //process considered crashed
        }
        return this.max;
    }
}
