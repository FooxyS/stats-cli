package com.volgoblob.internal.infrastructure.aggregation.aggregators;

import java.util.HashSet;
import java.util.Set;

import com.volgoblob.internal.domain.interfaces.aggregations.Aggregator;
import com.volgoblob.internal.infrastructure.aggregation.errors.AggregatorsException;

public class DcAggregator implements Aggregator {

    private Set<String> set = new HashSet<>();

    @Override
    public void add(Object value) {
        if (!value.getClass().equals(String.class)) throw new AggregatorsException("Passed argument is not string");
        String passedValue = (String) value;
        set.add(passedValue);
    }

    @Override
    public void combine(Aggregator aggregator) {
        if (!aggregator.getClass().equals(DcAggregator.class)) throw new AggregatorsException("Passed class must match to current class");
        DcAggregator passedAgg = (DcAggregator) aggregator;
        set.addAll(passedAgg.getSet());
    }

    @Override
    public Object finish() {
        return set.size();
    }
    
    public Set<String> getSet() {
        return set;
    }

}
