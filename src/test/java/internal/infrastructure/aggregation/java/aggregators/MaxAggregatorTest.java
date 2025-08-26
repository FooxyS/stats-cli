package internal.infrastructure.aggregation.java.aggregators;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import com.volgoblob.internal.infrastructure.aggregation.java.aggregators.DcAggregator;
import com.volgoblob.internal.infrastructure.aggregation.java.aggregators.MaxAggregator;
import com.volgoblob.internal.infrastructure.aggregation.java.errors.AggregatorsException;

public class MaxAggregatorTest {

    MaxAggregator aggregator = new MaxAggregator();

    @AfterEach
    void reset() {
        aggregator.reset();
    }

    @Test
    void addTest_argIsNotNumber_throws() {
        // preparing
        String arg = "argument";

        // assert
        assertThrows(AggregatorsException.class, () -> aggregator.add(arg), "Passed value is not instance of Number.");
    }

    @Test
    void addTest_correctNumberValue_save() {
        // preparing
        Number arg = 777.0;

        // assert
        assertDoesNotThrow(() -> aggregator.add(arg));
        assertEquals(arg, aggregator.getMaxValue(), "Passed value was not save.");
        assertTrue(aggregator.hasValue(), "Did not switch hasValue flag.");
    }

    @ParameterizedTest
    @CsvSource({
        "333, 555",
        "777, 777"
    })
    void addTest_lessAndMoreValues_skipAndSave(double newValue, double expectedValue) {
        // preparing 
        aggregator.add(555);

        // act
        aggregator.add(newValue);

        // assert
        assertEquals(expectedValue, aggregator.getMaxValue(), "Unexpected behaviour in add method.");
    }

    @Test
    void combineTest_argIsNotMaxAggregator_throws() {
        // preparing
        DcAggregator dcAggregator = new DcAggregator();

        // assert
        assertThrows(AggregatorsException.class, () -> aggregator.combine(dcAggregator), "Value can be instance of MaxAggregator only.");
    }

    @Test
    void combineTest_maxAggIsEmpty_throws() {
        // preparing
        MaxAggregator maxAggregator = new MaxAggregator();

        // assert
        assertThrows(AggregatorsException.class, () -> aggregator.combine(maxAggregator), "Must throws if passed aggregator is empty.");
    }

    @Test
    void combineTest_correctArg_mergeValues() {
        // preparing
        Number arg1 = 555.0;
        Number arg2 = 777.0;
        MaxAggregator maxAggregator = new MaxAggregator();
        maxAggregator.add(arg2);

        aggregator.add(arg1);

        // assert
        assertDoesNotThrow(() -> aggregator.combine(maxAggregator), "Combine must take value that is instance of MaxAggregator.");
        assertEquals(arg2, aggregator.getMaxValue(), "Main aggregator is not update. Unexpected max value.");
    }

    @Test
    void finishTest_callWhenEmpty_throws() {
        // assert
        assertThrows(AggregatorsException.class, () -> aggregator.finish(), "Did not throws when aggregator is empty.");
    }

    @Test
    void finishTest_call_returnResult() {
        // preparing
        Number arg = 100;
        aggregator.add(arg);

        //act
        Number result = aggregator.finish();

        // assert
        assertEquals(arg, result.intValue(), "Result returned unexpected max value.");
    }

    @Test
    void resetTest_call_truncate() {
        // preparing
        Number arg = 100;
        aggregator.add(arg);

        // act
        aggregator.reset();

        // assert
        assertEquals(0.0, aggregator.getMaxValue(), "Reset did not truncate the max state.");
        assertFalse(aggregator.hasValue(), "Did not switch hasValue flag after reset.");
    }
}
