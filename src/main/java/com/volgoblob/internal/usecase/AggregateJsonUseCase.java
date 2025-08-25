package com.volgoblob.internal.usecase;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.volgoblob.internal.config.AppConfig;
import com.volgoblob.internal.domain.interfaces.aggregations.Aggregator;
import com.volgoblob.internal.domain.interfaces.aggregations.AggregatorForGroup;
import com.volgoblob.internal.domain.interfaces.aggregations.AggregatorsRegistry;
import com.volgoblob.internal.domain.interfaces.parsers.ParsersAdapter;
import com.volgoblob.internal.infrastructure.aggregation.java.errors.AggregatorsException;
import com.volgoblob.internal.utils.ValidationUtils;

/**
 * AggregateJsonUseCase is the main usecase of this application.
 */
public class AggregateJsonUseCase {
    private final ParsersAdapter parsersAdapter;
    private final AggregatorsRegistry aggregatorsRegistry;
    private final AggregatorForGroup groupAggregator;

    private boolean useNative;

    
    public AggregateJsonUseCase(ParsersAdapter parsersAdapter, AggregatorsRegistry aggregatorsRegistry, AggregatorForGroup groupAggregator) {
        this.parsersAdapter = parsersAdapter;
        this.aggregatorsRegistry = aggregatorsRegistry;
        this.groupAggregator = groupAggregator;
        this.useNative = Boolean.parseBoolean(AppConfig.getVariableFromConfig("USE_NATIVE"));
    }

    public AggregateJsonUseCase(ParsersAdapter parsersAdapter, AggregatorsRegistry aggregatorsRegistry, AggregatorForGroup groupAggregator, boolean useNative) {
        this.parsersAdapter = parsersAdapter;
        this.aggregatorsRegistry = aggregatorsRegistry;
        this.groupAggregator = groupAggregator;
        this.useNative = useNative;
    }

    /**
     * Execute method is used to run the business logic of this usecase.
     */
    public String execute(String aggregationName, String fieldName, List<String> groupFields, Path jsonPath) {
        ValidationUtils.checkNotNull(aggregationName, "aggregationName");
        ValidationUtils.checkNotBlank(aggregationName, "aggregationName");

        ValidationUtils.checkNotNull(fieldName, "fieldName");
        ValidationUtils.checkNotBlank(fieldName, "fieldName");

        ValidationUtils.checkNotNull(jsonPath, "jsonPath");

        String aggregationNameUpper = aggregationName.toUpperCase();
        Supplier<Aggregator> aggSupplier = aggregatorsRegistry.create(aggregationNameUpper);
        if (aggSupplier == null) throw new AggregatorsException("aggregatorsRegistry return null.");

        /**
         * flags for choosing a solution
         */
        boolean isGroup = !(groupFields == null || groupFields.isEmpty());

        if (isGroup) {

            groupAggregator.register(groupFields);
            Map<List<Object>, Number> resultMap = parsersAdapter.getJsonReader().readWithGroup(jsonPath, aggregationNameUpper, fieldName, groupAggregator);
            String pathToResultFile = parsersAdapter.getJsonWriter().writeResultToJson(aggregationNameUpper, groupFields, fieldName, resultMap);
            return "Creating is successful. Path to your file: " + pathToResultFile;

        } else if (useNative) {
            
            Number result = parsersAdapter.getNativeJsonReader().readNoGroup(jsonPath, aggregationNameUpper, fieldName, aggSupplier);
            return String.format("Result: %s. By field: %s. Func: %s. File: %s", result, fieldName, aggregationNameUpper, jsonPath);
        
        }

        Number result = parsersAdapter.getJsonReader().readNoGroup(jsonPath, aggregationNameUpper, fieldName, aggSupplier);
        return String.format("Result: %s. By field: %s. Func: %s. File: %s", result, fieldName, aggregationNameUpper, jsonPath);

    }

}
