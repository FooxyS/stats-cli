package com.volgoblob.internal.infrastructure.json.jackson;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.volgoblob.internal.domain.interfaces.parsers.JsonWriter;

public class JacksonJsonWriter implements JsonWriter {

    @Override
    public String writeResultToJson(String aggregationName, List<String> groupFields, String fieldName, Map<List<Object>, Number> resultMap) {
        try {
            List<Map<String, Object>> data = dataPreparation(aggregationName, groupFields, fieldName, resultMap);
    
            ObjectMapper obj = new ObjectMapper();
    
            obj.enable(SerializationFeature.INDENT_OUTPUT);

            obj.writeValue(new File("./results/output.json"), data);

            // must return filepath
            return "output.json";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error in writeResultToJson";
        }

    }

    private List<Map<String, Object>> dataPreparation(String aggregationName, List<String> groupFields, String fieldName, Map<List<Object>, Number> resultMap) {

        List<Map<String, Object>> list = new ArrayList<>();

        for (Map.Entry<List<Object>, Number> entry : resultMap.entrySet()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(aggregationName.toLowerCase(), entry.getValue());
            for (int i = 0; i < groupFields.size(); i++) {
                List<Object> fieldsValue = entry.getKey();
                map.put(groupFields.get(i), fieldsValue.get(i));
            }
            list.add(map);
        }

        return list;
    }
    
}
