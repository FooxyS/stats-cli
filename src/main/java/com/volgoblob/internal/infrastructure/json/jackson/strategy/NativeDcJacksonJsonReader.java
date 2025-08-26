package com.volgoblob.internal.infrastructure.json.jackson.strategy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.volgoblob.internal.domain.interfaces.aggregations.AggType;
import com.volgoblob.internal.domain.interfaces.aggregations.Aggregator;
import com.volgoblob.internal.domain.interfaces.aggregations.AggregatorForGroup;
import com.volgoblob.internal.domain.interfaces.parsers.JsonReader;
import com.volgoblob.internal.infrastructure.json.jackson.errors.JsonParserException;

public class NativeDcJacksonJsonReader implements JsonReader {

    @Override
    public Number readNoGroup(Path jsonFile, String aggregationName, String fieldName, Supplier<Aggregator> supplier) {
        try (
            InputStream in = Files.newInputStream(jsonFile, StandardOpenOption.READ);
        ) {
            
            if (aggregationName == null || aggregationName.isBlank()) throw new JsonParseException("Incorrect aggregation name was passed");
            if (!aggregationName.equals(AggType.DC.name())) throw new JsonParseException("Unsupported aggregation name was passed. You only can use DC here.");

            Aggregator aggregator = supplier.get();

            JsonFactory factory = new JsonFactory();
            JsonParser parser = factory.createParser(in);

            if (parser.nextToken() != JsonToken.START_ARRAY) throw new JsonParseException("Input json is not array");

            readWhileNotEndArray(parser, aggregator, fieldName, aggregationName);

            

            parser.close();

            return aggregator.finish();

        } catch (Exception e) {
            throw new JsonParserException("Error in readNoGroup: ", e);
        }
    }

    private void readWhileNotEndObject(
        JsonParser parser, 
        Aggregator aggregator,
        String fieldName,
        String aggregationName
    ) throws IOException {

        if (parser.currentToken() != JsonToken.START_OBJECT) {
            parser.skipChildren();
            return;
        }

        while (parser.nextToken() != JsonToken.END_OBJECT) {

            String currentName = parser.currentName();
            parser.nextToken();

            if (currentName != null && currentName.equals(fieldName)) {
                if (aggregationName.equals(AggType.DC.name())) {
                    if (!parser.currentToken().equals(JsonToken.VALUE_NULL)) {
                        aggregator.add(parser.getValueAsString());
                    }
                }
            } else if (parser.currentToken().equals(JsonToken.START_OBJECT)) {
                readWhileNotEndObject(parser, aggregator, fieldName, aggregationName);
            } else if (parser.currentToken().equals(JsonToken.START_ARRAY)) {
                readWhileNotEndArray(parser, aggregator, fieldName, aggregationName);
            }
        }

    }

    private void readWhileNotEndArray(
        JsonParser parser,
        Aggregator aggregator,
        String fieldName,
        String aggregationName
    ) throws IOException {

        while (parser.nextToken() != JsonToken.END_ARRAY) {
            readWhileNotEndObject(parser, aggregator, fieldName, aggregationName);
        }

    }


    @Override
    public Map<List<Object>, Number> readWithGroup(Path jsonFile, String aggregationName, String fieldName,
            AggregatorForGroup groupAggregator) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'readWithGroup'");
    }
    
}
