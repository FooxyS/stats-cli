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
     * @throws AggregatorsException if passed value is not instance of string.
     */
    @Override
    public void add(Object value) {
        if (!(value instanceof String)) throw new AggregatorsException("Passed value is not instance of string");
        String passedValue = (String) value;
        set.add(passedValue);
    }

    /**
     * merge two identical aggregators.
     * @throws AggregatorsException if passed agg is not DcAggregator or null.
     */
    @Override
    public void combine(Aggregator aggregator) {
        if (!(aggregator instanceof DcAggregator)) throw new AggregatorsException("Combine is available with identical aggregator only");
        DcAggregator passedAgg = (DcAggregator) aggregator;
        set.addAll(passedAgg.getSet());
    }

    /**
     * return count of unique values (size of set).
     * @return Number type with set size.
     */
    @Override
    public Number finish() {
        return set.size();
    }
    
    /**
     * truncate set.
     */
    public void reset() {
        set.clear();
    }

    public Set<String> getSet() {
        return Set.copyOf(set);
    }

}
