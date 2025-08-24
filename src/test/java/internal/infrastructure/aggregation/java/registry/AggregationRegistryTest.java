package internal.infrastructure.aggregation.java.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.volgoblob.internal.domain.interfaces.aggregations.Aggregator;
import com.volgoblob.internal.infrastructure.aggregation.java.errors.AggregatorsException;
import com.volgoblob.internal.infrastructure.aggregation.java.registry.AggregationRegistry;

public class AggregationRegistryTest {
    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void constructorTest_flag_updateUseNative(boolean useNative) {
        // preparing
        AggregationRegistry registry = new AggregationRegistry(useNative);

        // act
        boolean result = registry.useNative();

        // assert
        assertEquals(useNative, result, "useNative flag was not updated from constructor.");
    }

    @ParameterizedTest
    @CsvSource({
        "false, AVG, AvgAggregator",
        "false, MAX, MaxAggregator",
        "false, DC, DcAggregator",
        "false, AVG, AvgAggregator",
        "true, DC, NativeDc"
    })
    void createTest_useNativeOfDefaule_returnNativeOrDefault(boolean useNative, String aggName, String simpleClassName) {
        // preparing
        AggregationRegistry registry = new AggregationRegistry(useNative);

        // act
        Aggregator result = registry.create(aggName).get();

        // assert
        assertEquals(simpleClassName, result.getClass().getSimpleName(), "Returned aggregation is not match to requested aggregation.");
    }

    @Test
    void createPrimitiveTest_uncorrectAggName_throws() {
        // preparing
        String unsupportedAgg = "UWU";
        AggregationRegistry registry = new AggregationRegistry(false); // false because primitive is for default

        // assert
        AggregatorsException e = assertThrows(AggregatorsException.class, () -> registry.create(unsupportedAgg), "Did not throw when upsupported agg was passed.");
        assertTrue(e.getMessage().contains(unsupportedAgg));
    }

    @ParameterizedTest
    @CsvSource({
        "false, AVG, AvgAggregator",
        "false, MAX, MaxAggregator",
        "false, DC, DcAggregator"
    })
    void createPrimitiveTest_supportedNames_expectedResult(boolean useNative, String aggName, String simpleClassName) {
        // preparing
        AggregationRegistry registry = new AggregationRegistry(useNative);

        // act
        Aggregator result = registry.create(aggName).get();

        // assert
        assertEquals(simpleClassName, result.getClass().getSimpleName(), "Returned aggregation is not match to requested aggregation.");
    }

    @Test
    void createNativeTest_uncorrectAggName_throws() {
        // preparing
        String unsupportedAgg = "UWU";
        AggregationRegistry registry = new AggregationRegistry(true); // true because it uses for native

        // assert
        AggregatorsException e = assertThrows(AggregatorsException.class, () -> registry.create(unsupportedAgg), "Did not throw when upsupported agg was passed.");
        assertTrue(e.getMessage().contains(unsupportedAgg));
    }

    @ParameterizedTest
    @CsvSource({
        "true, DC, NativeDc"
    })
    void createNativeTest_supportedNames_expectedResult(boolean useNative, String aggName, String simpleClassName) {
        // preparing
        AggregationRegistry registry = new AggregationRegistry(useNative);

        // act
        Aggregator result = registry.create(aggName).get();

        // assert
        assertEquals(simpleClassName, result.getClass().getSimpleName(), "Returned aggregation is not match to requested aggregation.");
    }
}
