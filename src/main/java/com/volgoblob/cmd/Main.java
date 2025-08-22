package com.volgoblob.cmd;


import java.nio.file.Path;

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

import jdk.jfr.Configuration;
import jdk.jfr.Recording;
import picocli.CommandLine;

public class Main {
    public static void main(String[] args) {
        try (
            Recording r = new Recording(Configuration.getConfiguration("profile"));
        ) {
            
            r.start();

            JsonReader defaultReader = new JacksonJsonReader();
            JsonReader nativeReader = new NativeDcJacksonJsonReader();
            JsonWriter writer = new JacksonJsonWriter();
            ParsersAdapter parsersAdapter = new JsonParserAdapter(defaultReader, nativeReader, writer);

            AggregationRegistry registry = new AggregationRegistry();

            GroupAggregator groupAggregator = new GroupAggregator();
            AggregateJsonUseCase usecase = new AggregateJsonUseCase(parsersAdapter, registry, groupAggregator);

            int exitCode = new CommandLine(new CliAdapter(usecase)).execute(args);
            
            r.stop();

            Path profilerDumpPath = Path.of("reports/profiling/latest-test.jfr");

            r.dump(profilerDumpPath);

            System.out.println("Profiler report saved to: reports/profiling/latest-test.jfr");

            System.exit(exitCode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}