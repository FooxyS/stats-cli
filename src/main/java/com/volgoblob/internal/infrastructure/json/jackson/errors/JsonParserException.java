package com.volgoblob.internal.infrastructure.json.jackson.errors;

public class JsonParserException extends RuntimeException {
    public JsonParserException(String message) {
        super(message);
    }

    public JsonParserException(String message, Throwable cause) {
        super(message, cause);
    }
}
