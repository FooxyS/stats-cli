package com.volgoblob.internal.infrastructure.aggregation.java.aggregators;

import com.volgoblob.internal.domain.interfaces.aggregations.Aggregator;
import com.volgoblob.internal.infrastructure.aggregation.java.errors.AggregatorsException;

public class AvgAggregator implements Aggregator {

    private double sum;
    private long count;

    /**
     * must accept value for aggregation and accumulate it inside
     * @param value must be Number
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
     */
    @Override
    public void combine(Aggregator aggregator) {
        if (!aggregator.getClass().equals(AvgAggregator.class)) throw new AggregatorsException("Combine is available with identical aggregator");
        AvgAggregator inputAgg = (AvgAggregator) aggregator;
        sum += inputAgg.getSum();
        count += inputAgg.getCount();
    }

    @Override
    public Object finish() {
        if (count == 0) throw new AggregatorsException("Attempt to calculate the result when denominator is zero");
        return sum/count;
    }

    public double getSum() {
        return sum;
    }

    public long getCount() {
        return count;
    }
}
