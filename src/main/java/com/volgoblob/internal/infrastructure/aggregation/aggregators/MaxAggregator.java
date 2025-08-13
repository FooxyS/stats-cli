package com.volgoblob.internal.infrastructure.aggregation.aggregators;

import com.volgoblob.internal.domain.interfaces.aggregations.Aggregator;
import com.volgoblob.internal.infrastructure.aggregation.errors.AggregatorsException;

public class MaxAggregator implements Aggregator {

    private double maxValue;
     
    @Override
    public void add(Object value) {
        if (!(value instanceof Number)) throw new AggregatorsException("Passed value is not instance of Number");
        Number passedObj = (Number) value;
        double passedValue = passedObj.doubleValue();
        if (passedValue > maxValue) {
            maxValue = passedValue;
        }
    }

    @Override
    public void combine(Aggregator aggregator) {
        if (!aggregator.getClass().equals(MaxAggregator.class)) throw new AggregatorsException("Combine is available with identical aggregator");
        MaxAggregator passedAgg = (MaxAggregator) aggregator;
        double passedValue = passedAgg.getMaxValue();
        if (passedValue > maxValue) {
            maxValue = passedValue;
        }
    }

    @Override
    public Object finish() {
        return maxValue;
    }

    public double getMaxValue() {
        return maxValue;
    }
    
}
