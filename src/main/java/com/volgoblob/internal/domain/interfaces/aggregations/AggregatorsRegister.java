package com.volgoblob.internal.domain.interfaces.aggregations;

import java.util.function.Supplier;

/**
 * AggregatorsRegister interface returns the factory of aggregator by its name.
 */
public interface AggregatorsRegister {
    Supplier<Aggregator> create(String name);
}
