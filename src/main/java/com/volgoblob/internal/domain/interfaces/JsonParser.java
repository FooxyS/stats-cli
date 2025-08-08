package com.volgoblob.internal.domain.interfaces;

import java.util.List;
import java.util.Map;

/**
 * JsonParser interface defines the methods that will be used in the usecase.
 */
public interface JsonParser {
    Map<List<String>, List<Object>> batchFromJson(String jsonFile, List<String> groupFields, String fieldName); // parses the json in batch mode
    String buildResultJson(Map<List<String>, List<Object>> map, List<String> groupFields, String fieldName);
}
