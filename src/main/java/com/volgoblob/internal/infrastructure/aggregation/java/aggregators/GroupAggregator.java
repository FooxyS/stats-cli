package com.volgoblob.internal.infrastructure.aggregation.java.aggregators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.volgoblob.internal.domain.interfaces.aggregations.AggType;
import com.volgoblob.internal.infrastructure.aggregation.java.errors.AggregatorsException;

/**
 * <b>WARNING:</b> This class be fully refactored. Use it carefully.
 * 
 * Used to do aggregations by group of specified keys. Support Avg, Max and Dc only.
 */
public class GroupAggregator {

    // result map and also used for max
    private Map<List<Object>, Number> map = new HashMap<>();
    
    // avg map
    private Map<List<Object>, AvgState> avgMap = new HashMap<>();

    // dc map
    private Map<List<Object>, Set<String>> dcMap = new HashMap<>();
    
    private List<String> listOfGroup;

    /**
     * read list size for further creating fixed size list as key of result map
     * @param groupList list of json fields group
     * @throws AggregatorsException if passed list of keys is empty
     */
    public void register(List<String> groupList) {
        if (groupList.isEmpty()) throw new AggregatorsException("Passed list of fields group is empty.");
        listOfGroup = groupList;
    }

    /**
     * return true if group list contain specified name. And false if not.
     * @param name json field name
     * @return true or false if list contain specified name
     */
    public boolean groupContainField(String name) {
        return listOfGroup.contains(name);
    }

    /**
     * create instance of list with fixed size that equal to group list size. 
     * @return list with size that equals the group list size.
     * @throws AggregatorsException if group list is empty.
     */
    public List<Object> getListFixedSize() {
        if (listOfGroup.isEmpty()) throw new AggregatorsException("listOfGroup is empty. Can't create instance of fixed list");
        return new ArrayList<>(listOfGroup.size());
    }

    /**
     * return index of fieldName in groupList
     * @param fieldName json field name
     * @return index of fieldName in groupList or -1 if not exist
     */
    public int fieldGroupIdx(String fieldName) {
        return listOfGroup.indexOf(fieldName);
    }

    /**
     * update avg state
     * @param key list with values from json in the order in which we have the group list
     * @param sum sum value to recording
     * @param count count value to recording
     */
    public void updateAvg(List<Object> key, double sum, int count) {
        avgMap.computeIfAbsent(key, k -> new AvgState()).updateState(sum, count);
    }

    /**
     * update max state
     * @param key list with values from json in the order in which we have the group list
     * @param value max value to recording
     */
    public void updateMax(List<Object> key, Number value) {
        map.merge(key, value, (oldValue, newValue) -> {
            if (newValue.doubleValue() > oldValue.doubleValue()) {
                return newValue;
            }
            return oldValue;
        });
    }

    /**
     * update dc state
     * @param key list with values from json in the order in which we have the group list
     * @param input set of string values from json 
     */
    public void updateDc(List<Object> key, Set<String> input) {
        dcMap.computeIfAbsent(key, k -> new HashSet<>()).addAll(input);
    }

    /**
     * return result. Map with key of json values that mapped to calculated aggregation.
     * @param aggregationName name of aggregation you want return
     * @return result by name of aggregation or empty map if aggregation is not support. Available types you can see in {@link AggType}.
     */
    public Map<List<Object>, Number> finish(String aggregationName) {
        switch (aggregationName) {
            case "AVG" -> {
                return avgMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> (Number) e.getValue().calculateResult()));
            }
            case "MAX" -> {
                return map;
            }
            case "DC" -> {
                return dcMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> (Number) e.getValue().size()));
            }

            default -> {
                return new HashMap<>();
            }
        }
    }

    /**
     * Avg state. Used to keep sum and count in map.
     */
    private class AvgState {
        
        double avgSum = 0.0;
        int avgCount = 0;

        private AvgState() {}

        private AvgState(double avgSum, int avgCount) {
            this.avgSum = avgSum;
            this.avgCount = avgCount;
        }

        private void updateState(double newSum, int newCount) {
            
            avgSum += newSum;
            avgCount += newCount;

        }

        private double calculateResult() {
            if (avgCount == 0) throw new AggregatorsException("Attempt to calculate the result when denominator is zero");
            return avgSum / avgCount;
        }
        
    }
}