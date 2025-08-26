package com.volgoblob.internal.infrastructure.json.jackson.registry;

import com.volgoblob.internal.domain.interfaces.parsers.JsonReader;
import com.volgoblob.internal.domain.interfaces.parsers.JsonWriter;
import com.volgoblob.internal.domain.interfaces.parsers.ParsersAdapter;
import com.volgoblob.internal.infrastructure.json.jackson.errors.JsonParserException;

public class JsonParserAdapter implements ParsersAdapter {

    JsonReader defaultJsonReader;
    JsonReader nativeJsonReader;
    JsonWriter jsonWriter;

    public JsonParserAdapter(JsonReader defaultJsonReader, JsonReader nativeJsonReader, JsonWriter jsonWriter) {
        this.defaultJsonReader = defaultJsonReader;
        this.nativeJsonReader = nativeJsonReader;
        this.jsonWriter = jsonWriter;
    }

    @Override
    public JsonReader getJsonReader() {
        if (defaultJsonReader == null) throw new JsonParserException("defaultJsonReader is null. Please inject the implementaion.");
        return defaultJsonReader;
    }

    @Override
    public JsonReader getNativeJsonReader() {
        if (nativeJsonReader == null) throw new JsonParserException("nativeJsonReader is null. Please inject the implementaion.");
        return nativeJsonReader;
    }

    @Override
    public JsonWriter getJsonWriter() {
        if (jsonWriter == null) throw new JsonParserException("jsonWriter is null. Please inject the implementaion.");
        return jsonWriter;
    }
    
}
