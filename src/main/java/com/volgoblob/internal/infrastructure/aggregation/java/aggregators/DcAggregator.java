package com.volgoblob.internal.infrastructure.aggregation.java.aggregators;

import java.util.HashSet;
import java.util.Set;

import com.volgoblob.internal.domain.interfaces.aggregations.Aggregator;
import com.volgoblob.internal.infrastructure.aggregation.java.errors.AggregatorsException;

/**
 * This aggregator contain set of passed values to count unique values. Supports add, combine, finish, getSet methods.
 */
public class DcAggregator implements Aggregator {

    // set of unique passed values.
    private Set<String> set = new HashSet<>();

    /**
     * add new value into set of unique values
     * @throws AggregatorsException if passed arg is not string.
     */
    @Override
    public void add(Object value) {
        if (!value.getClass().equals(String.class)) throw new AggregatorsException("Passed argument is not string");
        String passedValue = (String) value;
        set.add(passedValue);
    }

    /**
     * merge two identical aggregators.
     * @throws AggregatorsException if passed agg is not DcAggregator or null.
     */
    @Override
    public void combine(Aggregator aggregator) {
        if (aggregator == null || !aggregator.getClass().equals(DcAggregator.class)) throw new AggregatorsException("Passed class must match to current class");
        DcAggregator passedAgg = (DcAggregator) aggregator;
        set.addAll(passedAgg.getSet());
    }

    /**
     * return count of unique values (size of set).
     * @return Number set size.
     */
    @Override
    public Number finish() {
        return set.size();
    }
    
    public Set<String> getSet() {
        return set;
    }

}
