package com.volgoblob.internal.domain.interfaces.parsers;

import java.util.List;
import java.util.Map;

public interface JsonWriter {
    String writeResultToJson(String aggregationName, List<String> groupFields, String fieldName, Map<List<String>, Number> resultMap);
}
