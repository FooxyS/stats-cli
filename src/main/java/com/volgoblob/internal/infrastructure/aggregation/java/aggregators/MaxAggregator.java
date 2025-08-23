package com.volgoblob.internal.infrastructure.aggregation.java.aggregators;

import com.volgoblob.internal.domain.interfaces.aggregations.Aggregator;
import com.volgoblob.internal.infrastructure.aggregation.java.errors.AggregatorsException;

/**
 * This aggregator is used to find max value. Supports add, combine, finish.
 */
public class MaxAggregator implements Aggregator {

    // contain state of max value
    private double maxValue = 0;
    private boolean hasValue = false;
     
    /**
     * check if passed value more or less then actual state. If value more - update state.
     * @throws AggregatorsException if passed value is not instance of Number
     */
    @Override
    public void add(Object value) {
        if (!(value instanceof Number)) throw new AggregatorsException("Expected type is Number, but got: " + value.getClass().getSimpleName());
        Number number = (Number) value;
        updateMax(number.doubleValue());
    }

    /**
     * merge values of two identical aggregators.
     * @throws AggregatorsException if passed aggregator is not instance of MaxAggregator.
     */
    @Override
    public void combine(Aggregator aggregator) {
        if (!aggregator.getClass().equals(MaxAggregator.class)) throw new AggregatorsException("Combine is available with identical aggregator");
        MaxAggregator passedAgg = (MaxAggregator) aggregator;
        if (!passedAgg.hasValue()) throw new AggregatorsException("Passed aggregator is empty.");
        updateMax(passedAgg.getMaxValue());
    }

    /**
     * return max value from state.
     * @return max value from state
     */
    @Override
    public Number finish() {
        if (!hasValue) throw new AggregatorsException("Max aggregator is empty. Cannot return result.");
        return maxValue;
    }

    /**
     * truncate state. Max = 0.
     */

    public double getMaxValue() {
        return maxValue;
    }

    public boolean hasValue() {
        return hasValue;
    }
    
}
