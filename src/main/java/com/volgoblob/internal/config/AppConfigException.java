package com.volgoblob.internal.config;

public class AppConfigException extends RuntimeException {

    public AppConfigException(String meesage) {
        super(meesage);
    }
    
    public AppConfigException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
