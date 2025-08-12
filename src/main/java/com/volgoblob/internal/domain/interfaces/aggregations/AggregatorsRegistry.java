package com.volgoblob.internal.domain.interfaces.aggregations;

import java.util.function.Supplier;

/**
 * AggregatorsRegister interface returns the factory of aggregator by its name.
 */
public interface AggregatorsRegistry {
    Supplier<Aggregator> create(String name);
}
