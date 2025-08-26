package com.volgoblob.internal.domain.interfaces.aggregations;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * interface for group aggregation.
 */
public interface AggregatorForGroup {
    void register(List<String> groupList);
    boolean groupContainField(String name);
    List<Object> getListFixedSize();
    int fieldGroupIdx(String fieldName);
    void updateAvg(List<Object> key, double sum, int count);
    void updateMax(List<Object> key, Number value);
    void updateDc(List<Object> key, Set<String> input);
    Map<List<Object>, Number> finish(String aggregationName);
}
