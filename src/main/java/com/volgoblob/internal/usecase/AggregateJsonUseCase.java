package com.volgoblob.internal.usecase;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.volgoblob.internal.domain.interfaces.aggregations.Aggregator;
import com.volgoblob.internal.domain.interfaces.aggregations.AggregatorsRegistry;
import com.volgoblob.internal.domain.interfaces.parsers.JsonReader;
import com.volgoblob.internal.domain.interfaces.parsers.JsonWriter;
import com.volgoblob.internal.domain.interfaces.profiler.Profiler;

/**
 * AggregateJsonUseCase is the main usecase of this application.
 */
public class AggregateJsonUseCase {
    private final JsonReader jsonReader;
    private final JsonWriter jsonWriter;
    private final AggregatorsRegistry aggregatorsRegistry;
    private final Profiler profiler;

    public AggregateJsonUseCase(JsonReader jsonReader, JsonWriter jsonWriter, AggregatorsRegistry aggregatorsRegistry, Profiler profiler) {
        this.jsonReader = jsonReader;
        this.jsonWriter = jsonWriter;
        this.aggregatorsRegistry = aggregatorsRegistry;
        this.profiler = profiler;
    }

    /**
     * Execute method is used to run the business logic of this usecase.
     */
    public String execute(String aggregationName, String fieldName, List<String> groupFields, String jsonFile) {
        Supplier<Aggregator> aggSupplier = aggregatorsRegistry.create(aggregationName);

        if (groupFields == null || groupFields.isEmpty()) {
            profiler.start("readJsonNoGroup timer");
            Number result = jsonReader.readNoGroup(jsonFile, fieldName, aggSupplier);
            profiler.stop("readJsonNoGroup timer");
            return String.format("Result: %s. By field: %s. Func: %s. File: %s", result, fieldName, aggregationName, jsonFile);
        } else {
            Map<List<String>, Number> resultMap = jsonReader.readWithGroup(jsonFile, groupFields, fieldName, aggSupplier);
            String pathToResultFile = jsonWriter.writeResultToJson(aggregationName, groupFields, fieldName, resultMap);
            return "Creating is successful. Path to your file: " + pathToResultFile;
        }        
    
    }

}
