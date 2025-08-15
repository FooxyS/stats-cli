package com.volgoblob.cmd;


import com.volgoblob.internal.adapters.CliAdapter;
import com.volgoblob.internal.domain.interfaces.parsers.JsonReader;
import com.volgoblob.internal.domain.interfaces.parsers.JsonWriter;
import com.volgoblob.internal.domain.interfaces.profiler.Profiler;
import com.volgoblob.internal.infrastructure.aggregation.registry.AggregationRegistry;
import com.volgoblob.internal.infrastructure.json.jackson.JacksonJsonReader;
import com.volgoblob.internal.infrastructure.json.jackson.JacksonJsonWriter;
import com.volgoblob.internal.infrastructure.profiling.jvm.JvmProfiler;
import com.volgoblob.internal.usecase.AggregateJsonUseCase;

import picocli.CommandLine;

public class Main {
    public static void main(String[] args) {
        JsonReader reader = new JacksonJsonReader();
        JsonWriter writer = new JacksonJsonWriter();
        AggregationRegistry registry = new AggregationRegistry();
        Profiler profiler = new JvmProfiler();
        AggregateJsonUseCase usecase = new AggregateJsonUseCase(reader, writer, registry, profiler);
        int exitCode = new CommandLine(new CliAdapter(usecase)).execute(args);
        System.exit(exitCode);
    }
}