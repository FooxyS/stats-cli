package internal.infrastructure;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import com.volgoblob.internal.infrastructure.profiling.jvm.JvmProfiler;
import com.volgoblob.internal.infrastructure.profiling.jvm.errors.ProfilerException;

@TestInstance(Lifecycle.PER_CLASS)
public class JvmProfilerTest {
    
    private JvmProfiler profiler;

    @BeforeAll
    void setup() {
        profiler = new JvmProfiler();
    }

    @BeforeEach
    void resetMaps() {
        profiler.reset();
    }

    @Test
    void happyPath() {
        String testKey = "test";
        
        profiler.start(testKey);
        profiler.stop(testKey);
        Map<String, Long> map = profiler.result();

        assertTrue(map.containsKey(testKey), "Profiler should have key in the result map.");
        assertTrue(map.get(testKey) != null && map.get(testKey) > 0, "Profiler should have nanosec in the result map");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void startTest_whenNullOrBlankKey_throw(String testKey) {
        assertThrows(ProfilerException.class, () -> profiler.start(testKey), "Start() should throws exception when key is null or blank.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void stopTest_whenNullOrBlankKey_throw(String testKey) {
        assertThrows(ProfilerException.class, () -> profiler.stop(testKey), "Stop() should throws when key is null or blank.");
    }
    
    @Test
    void startTest_keyAlreadyMapped_throw() {
        String key = "serialization";
        profiler.start(key);

        ProfilerException e = assertThrows(ProfilerException.class, () -> profiler.start(key), "Start() must throws exception when key is already mapped in the intermediate map.");

        assertTrue(e.getMessage().contains(key), "Exception's message must contains key name.");
    }

    @Test
    void startTest_happyPath() {
        String key = "happy";

        profiler.start(key);
        Map<String, Long> map = profiler.inProgressKeys();

        assertTrue(map.containsKey(key) && map.get(key) > 0, "Start() should mapped specified key and associated value.");
    }

    @Test
    void stopTest_keyIsMissing_throw() {
        String key = "keyToStopTimer";

        ProfilerException e = assertThrows(ProfilerException.class, () -> profiler.stop(key), "Profiler should throws when there is no started timer with specified key.");
        assertTrue(e.getMessage().contains(key), "Error message should contains specified key");
    }

    @Test
    void stopTest_happyPath() {
        String key = "happyPathInStopKey";
        profiler.start(key);

        profiler.stop(key);
        Map<String, Long> resultMap = profiler.result();

        assertThrows(ProfilerException.class, () -> profiler.inProgressKeys(), "Mapped key must be removed in stop(). Profiler should throws exception because there is no any key.");
        assertTrue(resultMap.containsKey(key), "Profiler.stop() should put the stopped timer to the result map.");
    }

    @Test
    void stopTest_twoTimers_sumValues() {
        String key = "sumTimersKey";

        // first timer.
        profiler.start(key);
        profiler.stop(key);

        // get first timer value
        Map<String, Long> firstResultMap = profiler.result();
        Long oldValue = firstResultMap.get(key);

        // second timer
        profiler.start(key);
        profiler.stop(key);
        Map<String, Long> resultMap = profiler.result();
        Long newValue = resultMap.get(key);

        assertTrue(newValue > oldValue, "Profiler stop method must merge timers with the same key.");
    }

    @Test
    void resultTest_emptyResultMap_throw() {
        assertThrows(ProfilerException.class, () -> profiler.result(), "Profiler result() should throws exception when result map is empty.");
    }

    @Test
    void resultTest_unfinishedMeasurements_throw() {
        // any value to avoid empty result map error
        profiler.start("WOW");
        profiler.stop("WOW");

        // unfinished measurements
        String key = "OHNO";
        profiler.start(key);

        ProfilerException e = assertThrows(ProfilerException.class, () -> profiler.result(), "Profiler result() should throws when there are unfinished measurements");
        assertTrue(e.getMessage().contains(key));
    }

    @Test
    void resultTest_happyPath() {
        String key = "happyPathResultKey";
        profiler.start(key);
        profiler.stop(key);

        Map<String, Long> map = profiler.result();
        assertTrue(map.containsKey(key) && map.get(key) > 0, "Profiler result() should return the copy of result map. It must contains specified key and calculated time.");
    }

    @Test
    void inProgressKeysTest_emptyIntermediateMap_throw() {
        assertThrows(ProfilerException.class, () -> profiler.inProgressKeys(), "Profiler inProgressKeys() should throws when intermediate map has no started timers.");
    }

    @Test
    void inProgressKeysTest_happyPath() {
        String key = "Boom";
        profiler.start(key);

        Map<String, Long> map = profiler.inProgressKeys();

        assertTrue(map.containsKey(key) && map.get(key) > 0, "Profiler inProgressKeys() should returns the map with all unfinished timers.");
    }

    @Test
    void resetTest_resetInternediateMap_happyPath() {
        String key = "timer";
        profiler.start(key);

        profiler.reset();

        assertThrows(ProfilerException.class, () -> profiler.inProgressKeys(), "Profiler reset() should clears the intermediate map");
    }

    @Test
    void resetTest_resetResultMap_happyPath() {
        String key = "wowTimer";
        profiler.start(key);
        profiler.stop(key);

        profiler.reset();

        assertThrows(ProfilerException.class, () -> profiler.result(), "Profiler reset() should clears the result map");
    }
    // TODO: Разбить сложные ассерты на маленькие
    // TODO: пересмотреть логику inProgressKeys(), потому что у нас метод stop зависит от этого метода. То есть, если у нас поменяется логика inProgressKeys(), то могут упасть тесты stop().
}
