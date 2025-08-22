package com.volgoblob.internal.infrastructure.aggregation.java.registry;

import java.util.Map;
import java.util.function.Supplier;

import com.volgoblob.internal.config.AppConfig;
import com.volgoblob.internal.domain.interfaces.aggregations.AggType;
import com.volgoblob.internal.domain.interfaces.aggregations.Aggregator;
import com.volgoblob.internal.domain.interfaces.aggregations.AggregatorsRegistry;
import com.volgoblob.internal.infrastructure.aggregation.java.aggregators.AvgAggregator;
import com.volgoblob.internal.infrastructure.aggregation.java.aggregators.DcAggregator;
import com.volgoblob.internal.infrastructure.aggregation.java.aggregators.MaxAggregator;
import com.volgoblob.internal.infrastructure.aggregation.java.errors.AggregatorsException;
import com.volgoblob.internal.infrastructure.aggregation.nativeGo.aggregators.NativeDc;

public class AggregationRegistry implements AggregatorsRegistry {

    private final boolean useNative = Boolean.parseBoolean(AppConfig.getVariableFromConfig("USE_NATIVE"));

    private final Map<String, Supplier<Aggregator>> primitiveAggMap = Map.of(
        AggType.AVG.name(), AvgAggregator::new,
        AggType.MAX.name(), MaxAggregator::new,
        AggType.DC.name(), DcAggregator::new
    );

    private final Map<String, Supplier<Aggregator>> nativeAggMap = Map.of(
        AggType.DC.name(), NativeDc::new
    );


    @Override
    public Supplier<Aggregator> create(String name) {
        if (useNative) {
            System.out.println("вызвалась нативка");
            return createNative(name);
        }
        System.out.println("вызвалась обычная агрегация");
        return createPrimitive(name);
    }

    private Supplier<Aggregator> createPrimitive(String name) {
        Supplier<Aggregator> result = primitiveAggMap.get(name);
        if (result == null) throw new AggregatorsException("There is no aggregators with specified key. You can use: " + primitiveAggMap.keySet());
        return result;
    }

    private Supplier<Aggregator> createNative(String name) {
        Supplier<Aggregator> result = nativeAggMap.get(name);
        if (result == null) throw new AggregatorsException("There is no aggregators with specified key. You can use: " + nativeAggMap.keySet());
        return result;
    }

    
}
