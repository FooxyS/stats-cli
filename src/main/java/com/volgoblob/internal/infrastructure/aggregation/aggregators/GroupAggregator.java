package com.volgoblob.internal.infrastructure.aggregation.aggregators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.volgoblob.internal.infrastructure.aggregation.errors.AggregatorsException;

public class GroupAggregator {

    // result map
    private Map<List<Object>, Number> map = new HashMap<>();

    // avg map
    private Map<List<Object>, AvgState> avgMap = new HashMap<>();

    // dc map
    private Map<List<Object>, Set<String>> dcMap = new HashMap<>();
    
    private List<String> listOfGroup;

    /**
     * read list size for further creating fixed size list as key of result map
     * @param groupList list of json fields group
     */
    public void register(List<String> groupList) {
        if (groupList.isEmpty()) throw new AggregatorsException("Passed list of fields group is empty.");
        listOfGroup = groupList;
    }

    public boolean groupContainField(String name) {
        return listOfGroup.contains(name);
    }

    public List<Object> getListFixedSize() {
        if (listOfGroup.isEmpty()) throw new AggregatorsException("listOfGroup is empty. Can't create instance of fixed list");
        return new ArrayList<>(listOfGroup.size());
    }

    /**
     * for return index of passed element in groupList
     * @param fieldName
     * @return index of passed element or -1 if not exist
     */
    public int fieldGroupIdx(String fieldName) {
        return listOfGroup.indexOf(fieldName);
    }

    public void updateAvg(List<Object> key, double sum, int count) {
        avgMap.computeIfAbsent(key, k -> new AvgState()).updateState(sum, count);
    }

    public void updateMax(List<Object> key, Number value) {
        map.merge(key, value, (oldValue, newValue) -> {
            if (newValue.doubleValue() > oldValue.doubleValue()) {
                return newValue;
            }
            return oldValue;
        });
    }

    public void updateDc(List<Object> key, Set<String> input) {
        dcMap.computeIfAbsent(key, k -> new HashSet<>()).addAll(input);
    }

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