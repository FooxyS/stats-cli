package com.volgoblob.internal.infrastructure.aggregation.java.errors;

public class AggregatorsException extends RuntimeException {
    public AggregatorsException(String message) {
        super(message);
    }

    public AggregatorsException(String message, Throwable cause) {
        super(message);
    }
}
