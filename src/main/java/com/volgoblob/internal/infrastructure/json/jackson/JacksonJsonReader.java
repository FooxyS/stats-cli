package com.volgoblob.internal.infrastructure.json.jackson;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.volgoblob.internal.domain.interfaces.aggregations.AggType;
import com.volgoblob.internal.domain.interfaces.aggregations.Aggregator;
import com.volgoblob.internal.domain.interfaces.parsers.JsonReader;
import com.volgoblob.internal.infrastructure.json.jackson.errors.JsonParserException;

public class JacksonJsonReader implements JsonReader {

    @Override
    public Number readNoGroup(Path jsonFile, String aggregationName, String fieldName, Supplier<Aggregator> supplier) {
        try (
            InputStream in = Files.newInputStream(jsonFile, StandardOpenOption.READ);
        ) {
            JsonFactory factory = new JsonFactory();
            JsonParser parser = factory.createParser(in);

            Aggregator result = supplier.get();
            Aggregator batchAgg = supplier.get();

            if (parser.nextToken() != JsonToken.START_ARRAY) throw new JsonParserException("Json file is not massive of objects");

            // TODO: вынести этот параметр в конфигурацию
            int BATCH_SIZE = 10000;
            int currentSize = 0;

            // TODO: добавить возможность чтения файлов, где есть вложенным массив, например, со строками.
            readWhileNotEndArray(parser, BATCH_SIZE, currentSize, result, batchAgg, supplier, fieldName, aggregationName);

            parser.close(); 

            result.combine(batchAgg);

            return (Number) result.finish();

        } catch (Exception e) {
            throw new JsonParserException("Error in readNoGroup: ", e);
        }
    }

    private BatchSizeState readWhileNotEndObject(
        JsonParser parser, 
        int BATCH_SIZE, 
        int currentSize, 
        Aggregator result, 
        Aggregator batchAgg, 
        Supplier<Aggregator> supplier,
        String fieldName,
        String aggregationName
    ) throws IOException {

        int thisCurrentSize = currentSize;
        Aggregator thisBatchAgg = batchAgg;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (thisCurrentSize >= BATCH_SIZE) {
                result.combine(thisBatchAgg);
                thisBatchAgg = supplier.get();
                thisCurrentSize = 0;
            }

            String currentName = parser.currentName();
            parser.nextToken();

            if (currentName.equals(fieldName)) {
                if (aggregationName.equals(AggType.AVG.name()) || aggregationName.equals(AggType.MAX.name())) {
                    if (parser.currentToken().isNumeric()) {
                        thisBatchAgg.add(parser.getNumberValue());
                        thisCurrentSize++;
                    }
                } else if (aggregationName.equals(AggType.DC.name())) {
                    if (!parser.currentToken().equals(JsonToken.VALUE_NULL)) {
                        thisBatchAgg.add(parser.getValueAsString());
                        thisCurrentSize++;
                    }
                }
            } else if (parser.currentToken().equals(JsonToken.START_OBJECT)) {
                BatchSizeState batchSizeState = readWhileNotEndObject(parser, BATCH_SIZE, thisCurrentSize, result, thisBatchAgg, supplier, fieldName, aggregationName);
                thisCurrentSize = batchSizeState.getCurrentSize();
                thisBatchAgg = batchSizeState.getNewBatchAgg();
            } else if (parser.currentToken().equals(JsonToken.START_ARRAY)) {
                BatchSizeState batchSizeState = readWhileNotEndArray(parser, BATCH_SIZE, thisCurrentSize, result, thisBatchAgg, supplier, fieldName, aggregationName);
                thisCurrentSize = batchSizeState.getCurrentSize();
                thisBatchAgg = batchSizeState.getNewBatchAgg();
            }
        }

        return new BatchSizeState(thisCurrentSize, thisBatchAgg);
    }

    private BatchSizeState readWhileNotEndArray(
        JsonParser parser, 
        int BATCH_SIZE, 
        int currentSize, 
        Aggregator result, 
        Aggregator batchAgg, 
        Supplier<Aggregator> supplier,
        String fieldName,
        String aggregationName
    ) throws IOException {

        int thisCurrentSize = currentSize;
        Aggregator thisBatchAgg = batchAgg;

        while (parser.nextToken() != JsonToken.END_ARRAY) {
            BatchSizeState batchSizeState = readWhileNotEndObject(parser, BATCH_SIZE, thisCurrentSize, result, thisBatchAgg, supplier, fieldName, aggregationName);
            thisCurrentSize = batchSizeState.getCurrentSize();
            thisBatchAgg = batchSizeState.getNewBatchAgg();
        }

        return new BatchSizeState(thisCurrentSize, thisBatchAgg);
    }

    private class BatchSizeState {
    
        private int currentSize;
        private Aggregator newBatchAgg;

        public BatchSizeState(int currentSize, Aggregator newBatchAgg) {
            this.currentSize = currentSize;
            this.newBatchAgg = newBatchAgg;
        }

        public int getCurrentSize() {
            return currentSize;
        }

        public Aggregator getNewBatchAgg() {
            return newBatchAgg;
        }

        @Override
        public String toString() {
            return "BatchSizeState [currentSize=" + currentSize + ", newBatchAgg=" + newBatchAgg + "]";
        }
    }

    @Override
    public Map<List<String>, Number> readWithGroup(Path jsonFile, List<String> groupFields, String fieldName,
            Supplier<Aggregator> supplier) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'readWithGroup'");
    }
    


}
