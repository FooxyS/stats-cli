package com.volgoblob.internal.infrastructure.aggregation.java.aggregators;

import com.volgoblob.internal.domain.interfaces.aggregations.Aggregator;
import com.volgoblob.internal.infrastructure.aggregation.java.errors.AggregatorsException;

/**
 * Used while reading json to save avg state of sum and count. Support add, combine, finish methods.
 */
public class AvgAggregator implements Aggregator {

    // sum of avg state
    private double sum;

    // count of avg state
    private long count;

    /**
     * check if passed value is Number, then plus it into avg state and increment count.
     * @param value must be Number
     * @throws AggregatorsException if passed value is not instance of Number.
     */
    @Override
    public void add(Object value) {
        if (!(value instanceof Number)) throw new AggregatorsException("Passed value is not instance of Number");
        Number number = (Number) value;
        sum += number.doubleValue();
        count++;
    }

    /**
     * method combine identical aggregators
     * @param aggregator the provided class must be equal to the current class (no subclassing allowed)
     * @throws AggregatorsException if provided class is not identical to current class
     */
    @Override
    public void combine(Aggregator aggregator) {
        if (!aggregator.getClass().equals(AvgAggregator.class)) throw new AggregatorsException("Combine is available with identical aggregator");
        AvgAggregator inputAgg = (AvgAggregator) aggregator;
        sum += inputAgg.getSum();
        count += inputAgg.getCount();
    }

    /**
     * return calculated result by actual state.
     * @return calculated result. Sum/count.
     * @throws AggregatorsException if denominator is zero.
     */
    @Override
    public Number finish() {
        if (count == 0) throw new AggregatorsException("Attempt to calculate the result when denominator is zero");
        return sum/count;
    }

    /**
     * truncate actual state. Sum = 0, count = 0.
     */
    public void reset() {
        sum = 0;
        count = 0;
    }

    public double getSum() {
        return sum;
    }

    public long getCount() {
        return count;
    }
}
