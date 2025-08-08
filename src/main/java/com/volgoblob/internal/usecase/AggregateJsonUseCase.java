package com.volgoblob.internal.usecase;

import java.util.List;
import java.util.Map;

import com.volgoblob.internal.domain.interfaces.AggregationFunctions;
import com.volgoblob.internal.domain.interfaces.JsonParser;
import com.volgoblob.internal.domain.interfaces.Profiler;

/**
 * AggregateJsonUseCase is the main usecase of this application.
 */
public class AggregateJsonUseCase {
    private final JsonParser jsonParser;
    private final AggregationFunctions aggregationFunctions;
    private final Profiler profiler;

    public AggregateJsonUseCase(JsonParser jsonParser, AggregationFunctions aggregationFunctions, Profiler profiler) {
        this.jsonParser = jsonParser;
        this.aggregationFunctions = aggregationFunctions;
        this.profiler = profiler;
    }

    /**
     * Execute method is used to run the business logic of this usecase.
     */
    public void execute(String aggregationName, String fieldName, List<String> groupFields, String jsonFile) {
        profiler.start("deserialization");
        Map<List<String>, List<Object>> map = jsonParser.batchFromJson(jsonFile, groupFields, fieldName);
        profiler.stop("deserialization");

        profiler.start("aggregation");
        aggregationFunctions.doAggregation(aggregationName, map);
        profiler.stop("aggregation");

        profiler.start("serialization");
        String result = jsonParser.buildResultJson(map, groupFields, fieldName);
        profiler.stop("serialization");

        System.out.println(result);
    }
}
