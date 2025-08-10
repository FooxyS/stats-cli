package com.volgoblob.internal.infrastructure.profiling.jvm.errors;

public class ProfilerException extends RuntimeException {
    public ProfilerException(String message) {
        super(message);
    }
}
