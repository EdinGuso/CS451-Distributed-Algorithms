package cs451.util.structures;

import java.util.TreeSet;
import java.util.Iterator;

import cs451.util.structures.Range;

/*
 * Data structure for efficient storing of large amount of elements
 * which have the property that they mostly appear in intervals
 * (i.e. not many unordered messages arrive). Supports add, remove,
 * contains, and maxOrdered operations, also implements iterator.
 */
public class RangeSet implements Iterable<Integer> {

    private TreeSet<Range> set;
    private Range current;
    private Range lower;
    private Range higher;
    
    public RangeSet() {
        this.set = new TreeSet<Range>();
    }

    public void add(int seq) {
        this.current = new Range(seq, seq);
        this.lower = this.set.lower(this.current);
        this.higher = this.set.higher(this.current);
        if (this.lower == null) {
            if (this.higher == null) {
                this.set.add(this.current);
                return;
            }
            if (seq == this.higher.getMin() - 1) {
                this.higher.setMin(seq);
                return;
            }
            this.set.add(this.current);
            return;
        }
        if (this.higher == null) {
            if (seq == this.lower.getMax() + 1) {
                this.lower.setMax(seq);
                return;
            }
            this.set.add(this.current);
            return;
        }
        if (seq == this.higher.getMin() - 1) {
            if (seq == this.lower.getMax() + 1) {
                this.set.remove(this.higher);
                this.lower.setMax(this.higher.getMax());
                return;
            }
            this.higher.setMin(seq);
            return;
        }
        if (seq == this.lower.getMax() + 1) {
            this.lower.setMax(seq);
            return;
        }
        this.set.add(this.current);
    }

    public void remove(int seq) {
        try {
            assert this.contains(seq);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.current = this.set.lower(new Range(seq, seq));
        if (this.current == null) {
            this.current = this.set.first();
        }
        else {
            this.current = this.set.higher(this.current);
        }
        if (seq == this.current.getMin()) {
            if (seq == this.current.getMax()) {
                this.set.remove(this.current);
                return;
            }
            this.current.setMin(seq + 1);
            return;
        }
        if (seq == this.current.getMax()) {
            this.current.setMax(seq - 1);
            return;
        }
        int first_max = this.current.getMax();
        this.current.setMax(seq - 1);
        this.set.add(new Range(seq + 1, first_max));
    }

    public boolean contains(int seq) {
        return this.set.contains(new Range(seq, seq));
    }

    public int maxOrdered() {
        if (this.set.isEmpty()) {
            return 0;
        }
        if (this.set.first().getMin() != 1) {
            return 0;
        }
        return this.set.first().getMax();
    }

    @Override
    public Iterator<Integer> iterator() {
        Iterator<Integer> it = new Iterator<Integer>() {

            Range itRange = new Range(-1, -1);
            int itValue = 0;

            @Override
            public boolean hasNext() {
                if (itValue <= itRange.getMax()) {
                    return true;
                }
                if (set.higher(itRange) != null) {
                    return true;
                }
                return false;
            }

            @Override
            public Integer next() {
                if (itValue > itRange.getMax()) {
                    itRange = set.higher(itRange);
                    itValue = itRange.getMin();
                }
                return itValue++;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
        return it;
    }
}
