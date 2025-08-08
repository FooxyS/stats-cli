package com.volgoblob.internal.domain.interfaces;

import java.util.List;
import java.util.Map;

/**
 * AggregationFunctions interface defines aggregation functions that will be used in the usecase.
 */
public interface AggregationFunctions {
    void doAggregation(String aggregationName, Map<List<String>, List<Object>> map);
    void max(Map<List<String>, List<Object>> map);
    void avg(Map<List<String>, List<Object>> map);
    void distinctCount(Map<List<String>, List<Object>> map);
}
