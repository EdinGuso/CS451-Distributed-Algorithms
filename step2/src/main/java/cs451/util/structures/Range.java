package cs451.util.structures;

import java.util.Comparator;

/*
 * Data structure for storing a range of integers
 */
public class Range implements Comparable<Range> {

    private int min;
    private int max;

    public Range(int min, int max) {
        try {
            assert min <= max;
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return this.min;
    }

    public int getMax() {
        return this.max;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public void setMax(int max) {
        this.max = max;
    }

    @Override
    public int compareTo(Range r) {
        if (this.getMax() < r.getMin()) {
            return -1;
        }
        if (this.getMin() > r.getMax()) {
            return 1;
        }
        return 0; //return equals even if we are just overlapping
    }
}
