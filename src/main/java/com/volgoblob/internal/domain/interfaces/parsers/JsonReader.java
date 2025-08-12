package com.volgoblob.internal.domain.interfaces.parsers;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.volgoblob.internal.domain.interfaces.aggregations.Aggregator;

/**
 * JsonParser interface defines the methods that will be used in the usecase.
 */
public interface JsonReader {
    Number readNoGroup(Path jsonFile, String aggregationName, String fieldName, Supplier<Aggregator> supplier);
    Map<List<String>, Number> readWithGroup(Path jsonFile, List<String> groupFields, String fieldName, Supplier<Aggregator> supplier);
}
