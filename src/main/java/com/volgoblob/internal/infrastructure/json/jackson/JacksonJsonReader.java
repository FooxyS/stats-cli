package com.volgoblob.internal.infrastructure.json.jackson;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.volgoblob.internal.config.AppConfig;
import com.volgoblob.internal.domain.interfaces.aggregations.AggType;
import com.volgoblob.internal.domain.interfaces.aggregations.Aggregator;
import com.volgoblob.internal.domain.interfaces.parsers.JsonReader;
import com.volgoblob.internal.infrastructure.aggregation.aggregators.GroupAggregator;
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

            int BATCH_SIZE = Integer.parseInt(AppConfig.getVariableFromConfig("BATCH_SIZE"));
            System.out.println("BATCH_SIZE is " + BATCH_SIZE);
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
    public Map<List<Object>, Number> readWithGroup(
        Path jsonFile, 
        String aggregationName,
        String fieldName,
        GroupAggregator groupAggregator
    ) {
        try (
            InputStream in = Files.newInputStream(jsonFile, StandardOpenOption.READ);
        ) {
            JsonFactory factory = new JsonFactory();
            JsonParser parser = factory.createParser(in);

            

            if (parser.nextToken() != JsonToken.START_ARRAY) throw new JsonParserException("Json file is not massive of objects");

            // TODO: добавить возможность чтения файлов, где есть вложенным массив, например, со строками.
            readWhileNotEndArrayForGroup(parser, groupAggregator, fieldName, aggregationName);
            parser.close(); 

            return groupAggregator.finish(aggregationName);

        } catch (Exception e) {
            throw new JsonParserException("Error in readWithGroup: ", e);
        }
    }
    
    private void readWhileNotEndObjectForGroup(
        JsonParser parser,
        GroupAggregator aggregator,
        String fieldName,
        String aggregationName
    ) throws IOException {

        try {
            // создаём список для ключа
            // достаём state, который надо накапливать
    
            double sum = 0;
            double max = 0;
            int count = 0;
            Set<String> set = new HashSet<>();
            List<Object> list = aggregator.getListFixedSize();
    
            while (parser.nextToken() != JsonToken.END_OBJECT) {
    
                String currentName = parser.currentName();
                parser.nextToken();
                
                if (currentName.equals(fieldName)) {
    
                    /**
                     * здесь прописана логика записи state для подсчёта
                     */
    
    
                    if (aggregationName.equals(AggType.AVG.name())) {
                        if (parser.currentToken().isNumeric()) {
                            sum += parser.getDoubleValue();
                            count++;
                        }
                    } else if (aggregationName.equals(AggType.MAX.name())) {
                        if (parser.currentToken().isNumeric()) {
                            double current = parser.getDoubleValue();
                            if (current > max) {
                                max = current;
                            }
                        } 
                    } else if (aggregationName.equals(AggType.DC.name())) {
                        if (!parser.currentToken().equals(JsonToken.VALUE_NULL)) {
                            set.add(parser.getValueAsString());
                        }
                    }
    
    
                } else if (aggregator.groupContainField(currentName)) {
    
                    int idx = aggregator.fieldGroupIdx(currentName);
                    list.add(idx, parser.getValueAsString());
    
                } else if (parser.currentToken().equals(JsonToken.START_OBJECT)) {
    
                    readWhileNotEndObjectForGroup(parser, aggregator, fieldName, aggregationName);
    
                } else if (parser.currentToken().equals(JsonToken.START_ARRAY)) {
    
                    readWhileNotEndArrayForGroup(parser, aggregator, fieldName, aggregationName);
    
                }
            }
    
            switch (aggregationName) {
                case "MAX" -> {
                    aggregator.updateMax(list, max);
                } 
                case "AVG" -> {
                    aggregator.updateAvg(list, sum, count);
                }                    
                case "DC" -> {
                    aggregator.updateDc(list, set);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void readWhileNotEndArrayForGroup(
        JsonParser parser, 
        GroupAggregator aggregator,
        String fieldName,
        String aggregationName
    ) throws IOException {


        while (parser.nextToken() != JsonToken.END_ARRAY) {
            readWhileNotEndObjectForGroup(parser, aggregator, fieldName, aggregationName);
        }

    }
    
}