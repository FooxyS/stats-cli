package com.volgoblob.internal.infrastructure.aggregation.java.aggregators;

import com.volgoblob.internal.domain.interfaces.aggregations.Aggregator;
import com.volgoblob.internal.infrastructure.aggregation.java.errors.AggregatorsException;

/**
 * This aggregator is used to find max value. Supports add, combine, finish.
 */
public class MaxAggregator implements Aggregator {

    // contain state of max value
     
    /**
     * check if passed value more or less then actual state. If value more - update state.
     * @throws AggregatorsException if passed value is not instance of Number
     */
    @Override
    public void add(Object value) {
    /**
     * merge values of two identical aggregators.
     * @throws AggregatorsException if passed aggregator is not instance of MaxAggregator.
     */
    @Override
    public void combine(Aggregator aggregator) {
        if (!aggregator.getClass().equals(MaxAggregator.class)) throw new AggregatorsException("Combine is available with identical aggregator");
        MaxAggregator passedAgg = (MaxAggregator) aggregator;
    /**
     * return max value from state.
     * @return max value from state
     */
    @Override
    public Number finish() {
        return maxValue;
    }

    /**
     * truncate state. Max = 0.
     */

    public double getMaxValue() {
        return maxValue;
    }
    
}
