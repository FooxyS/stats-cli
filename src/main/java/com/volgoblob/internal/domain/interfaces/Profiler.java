package com.volgoblob.internal.domain.interfaces;

/**
 * Profiler interface defines methods that will be used in the usecase.
 */
public interface Profiler {
    void start(String sectionName);
    void stop(String sectionName);
    void printResult();
}
