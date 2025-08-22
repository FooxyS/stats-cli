package com.volgoblob.internal.domain.interfaces.aggregations;

/**
 * Aggregator interface defines methods that Aggregators should inmplement
 */
public interface Aggregator {
    void add(Object value);
    void combine(Aggregator aggregator);
    Number finish();
}
