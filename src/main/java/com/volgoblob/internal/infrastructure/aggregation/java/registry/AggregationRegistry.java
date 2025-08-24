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

/**
 * registry contains all available aggregation. Default, native. As key is used list from {@link AggType}
 */
public class AggregationRegistry implements AggregatorsRegistry {

    // flag from config signals when should use native implementation.
    private boolean useNative;

    // TODO: убрать зависиомсть от config. Вынести в DI.
    public AggregationRegistry() {
        this.useNative = Boolean.parseBoolean(AppConfig.getVariableFromConfig("USE_NATIVE"));
    }

    public AggregationRegistry(boolean useNative) {
        this.useNative = useNative;
    }
    
    // all supported default aggregations.
    private final Map<String, Supplier<Aggregator>> primitiveAggMap = Map.of(
        AggType.AVG.name(), AvgAggregator::new,
        AggType.MAX.name(), MaxAggregator::new,
        AggType.DC.name(), DcAggregator::new
    );

    // all supported native aggregations.
    private final Map<String, Supplier<Aggregator>> nativeAggMap = Map.of(
        AggType.DC.name(), NativeDc::new
    );

    /**
     * return supplier with fabric method by specified key.
     * @param name name of aggregation you want recieve. If you want use native, change config.
     * @return return supplier with fabric method by specified key.
     */
    @Override
    public Supplier<Aggregator> create(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Passed name is incorrect. Name: " + name);
        if (useNative) {
            System.out.println("вызвалась нативка");
            return createNative(name);
        }
        System.out.println("вызвалась обычная агрегация");
        return createPrimitive(name);
    }

    /**
     * internal helper that is working with map of default aggregations.
     * @param name name of aggregation you want to recieve.
     * @return return supplier with fabric method by specified key.
     */
    private Supplier<Aggregator> createPrimitive(String name) {
        Supplier<Aggregator> result = primitiveAggMap.get(name);
        if (result == null) throw new AggregatorsException("There is no aggregators with specified key:" + name + "You can use: " + primitiveAggMap.keySet());
        return result;
    }

    /**
     * internal helper that is working with map of native aggregations.
     * @param name of aggregation you want to recieve.
     * @return return supplier with fabric method by specified key.
     */
    private Supplier<Aggregator> createNative(String name) {
        Supplier<Aggregator> result = nativeAggMap.get(name);
        if (result == null) throw new AggregatorsException("There is no aggregators with specified key:" + name + "You can use: " + nativeAggMap.keySet());
        return result;
    }

    public boolean useNative() {
        return useNative;
    }
}
