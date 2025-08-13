package com.volgoblob.internal.infrastructure.aggregation.registry;

import java.util.Map;
import java.util.function.Supplier;

import com.volgoblob.internal.domain.interfaces.aggregations.AggType;
import com.volgoblob.internal.domain.interfaces.aggregations.Aggregator;
import com.volgoblob.internal.domain.interfaces.aggregations.AggregatorsRegistry;
import com.volgoblob.internal.infrastructure.aggregation.aggregators.AvgAggregator;
import com.volgoblob.internal.infrastructure.aggregation.aggregators.DcAggregator;
import com.volgoblob.internal.infrastructure.aggregation.aggregators.MaxAggregator;

public class AggregationRegistry implements AggregatorsRegistry {

    private final Map<String, Supplier<Aggregator>> map = Map.of(
        AggType.AVG.name(), AvgAggregator::new,
        AggType.MAX.name(), MaxAggregator::new,
        AggType.DC.name(), DcAggregator::new
    );

    @Override
    public Supplier<Aggregator> create(String name) {
        return map.get(name);
    }
    
}
