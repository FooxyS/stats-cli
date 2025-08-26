package com.volgoblob.internal.domain.interfaces.parsers;

public interface ParsersAdapter {
    JsonReader getJsonReader();
    JsonReader getNativeJsonReader();
    JsonWriter getJsonWriter();
}
