package com.volgoblob.cmd;


import com.volgoblob.internal.adapters.CliAdapter;
import com.volgoblob.internal.domain.interfaces.parsers.JsonReader;
import com.volgoblob.internal.domain.interfaces.parsers.JsonWriter;
import com.volgoblob.internal.domain.interfaces.parsers.ParsersAdapter;
import com.volgoblob.internal.infrastructure.aggregation.java.aggregators.GroupAggregator;
import com.volgoblob.internal.infrastructure.aggregation.java.registry.AggregationRegistry;
import com.volgoblob.internal.infrastructure.json.jackson.registry.JsonParserAdapter;
import com.volgoblob.internal.infrastructure.json.jackson.strategy.JacksonJsonReader;
import com.volgoblob.internal.infrastructure.json.jackson.strategy.JacksonJsonWriter;
import com.volgoblob.internal.infrastructure.json.jackson.strategy.NativeDcJacksonJsonReader;
import com.volgoblob.internal.usecase.AggregateJsonUseCase;

import picocli.CommandLine;

public class Main {
    public static void main(String[] args) {
        try {

            JsonReader defaultReader = new JacksonJsonReader();
            JsonReader nativeReader = new NativeDcJacksonJsonReader();
            JsonWriter writer = new JacksonJsonWriter();
            ParsersAdapter parsersAdapter = new JsonParserAdapter(defaultReader, nativeReader, writer);

            AggregationRegistry registry = new AggregationRegistry();

            GroupAggregator groupAggregator = new GroupAggregator();
            AggregateJsonUseCase usecase = new AggregateJsonUseCase(parsersAdapter, registry, groupAggregator);

            int exitCode = new CommandLine(new CliAdapter(usecase)).execute(args);
            System.exit(exitCode);

        } catch (Exception e) {
            System.err.println("Error in Main:" + e.getMessage());
        }
    }
}