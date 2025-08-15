package com.volgoblob.internal.usecase;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.volgoblob.internal.domain.interfaces.aggregations.Aggregator;
import com.volgoblob.internal.domain.interfaces.aggregations.AggregatorsRegistry;
import com.volgoblob.internal.domain.interfaces.parsers.JsonReader;
import com.volgoblob.internal.domain.interfaces.parsers.JsonWriter;
import com.volgoblob.internal.domain.interfaces.profiler.Profiler;
import com.volgoblob.internal.infrastructure.aggregation.aggregators.GroupAggregator;

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
    public String execute(String aggregationName, String fieldName, List<String> groupFields, Path jsonFile) {
        String aggregationNameUpper = aggregationName.toUpperCase();
        Supplier<Aggregator> aggSupplier = aggregatorsRegistry.create(aggregationNameUpper);

        if (groupFields == null || groupFields.isEmpty()) {
            profiler.start("readJsonNoGroup timer");
            Number result = jsonReader.readNoGroup(jsonFile, aggregationNameUpper, fieldName, aggSupplier);
            profiler.stop("readJsonNoGroup timer");
            return String.format("Result: %s. By field: %s. Func: %s. File: %s", result, fieldName, aggregationNameUpper, jsonFile);
        } else {
            GroupAggregator groupAggregator = new GroupAggregator();
            groupAggregator.register(groupFields);
            Map<List<Object>, Number> resultMap = jsonReader.readWithGroup(jsonFile, aggregationNameUpper, fieldName, groupAggregator);
            String pathToResultFile = jsonWriter.writeResultToJson(aggregationNameUpper, groupFields, fieldName, resultMap);
            return "Creating is successful. Path to your file: " + pathToResultFile;
        }
    
    }

}
