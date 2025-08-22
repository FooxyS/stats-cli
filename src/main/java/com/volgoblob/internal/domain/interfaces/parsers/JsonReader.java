package com.volgoblob.internal.domain.interfaces.parsers;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.volgoblob.internal.domain.interfaces.aggregations.Aggregator;
import com.volgoblob.internal.infrastructure.aggregation.java.aggregators.GroupAggregator;

/**
 * JsonParser interface defines the methods that will be used in the usecase.
 */
public interface JsonReader {
    Number readNoGroup(Path jsonFile, String aggregationName, String fieldName, Supplier<Aggregator> supplier);
    Map<List<Object>, Number> readWithGroup(Path jsonFile, String aggregationName, String fieldName, GroupAggregator groupAggregator);
}
