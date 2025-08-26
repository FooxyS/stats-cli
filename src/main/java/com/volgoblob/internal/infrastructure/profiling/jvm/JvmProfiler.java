package com.volgoblob.internal.infrastructure.profiling.jvm;

import java.util.HashMap;
import java.util.Map;
import com.volgoblob.internal.infrastructure.profiling.jvm.errors.ProfilerException;

/**
 * JvmProfiler calculates execution time with System.nanoTime() method. Not thread-safe.
 */
public class JvmProfiler implements com.volgoblob.internal.domain.interfaces.profiler.Profiler{

    private final Map<String, Long> intermediateMap = new HashMap<>();
    private final Map<String, Long> resultMap = new HashMap<>();

    public JvmProfiler() {}

    @Override
    public void start(String sectionName) {
        checkKey(sectionName);
        if (intermediateMap.containsKey(sectionName)) throw new ProfilerException("Profiler with key '" + sectionName + "' was already used.");
        intermediateMap.put(sectionName, System.nanoTime());
    }

    @Override
    public void stop(String sectionName) {
        checkKey(sectionName);
        if (!intermediateMap.containsKey(sectionName)) throw new ProfilerException("Profiler with key '" + sectionName + "' was not used.");
        
        long startTime = intermediateMap.remove(sectionName);
        long elapsedTime = System.nanoTime() - startTime;

        resultMap.merge(sectionName, elapsedTime, Long::sum);
    }

    @Override
    public Map<String, Long> result() {
        if (resultMap.isEmpty()) throw new ProfilerException("Profiler has no any calculated time.");
        if (!intermediateMap.isEmpty()) throw new ProfilerException("There are unfinished measurements: " + intermediateMap.keySet());
        return Map.copyOf(resultMap);
    }

    public Map<String, Long> inProgressKeys() {
        if (intermediateMap.isEmpty()) throw new ProfilerException("Profiler has no started timers.");
        return Map.copyOf(intermediateMap);
    }

    /**
     * reset clear maps in profiler.
     */
    public void reset() {
        intermediateMap.clear();
        resultMap.clear();
    }
    
    private void checkKey(String sectionName) {
        if (sectionName == null) throw new ProfilerException("Key is null.");
        if (sectionName.isBlank()) throw new ProfilerException("Key is blank.");
    }
}
