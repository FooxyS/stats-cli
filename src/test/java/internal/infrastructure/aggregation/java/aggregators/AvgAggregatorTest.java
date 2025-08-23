package internal.infrastructure.aggregation.java.aggregators;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.volgoblob.internal.infrastructure.aggregation.java.aggregators.AvgAggregator;
import com.volgoblob.internal.infrastructure.aggregation.java.aggregators.MaxAggregator;
import com.volgoblob.internal.infrastructure.aggregation.java.errors.AggregatorsException;

public class AvgAggregatorTest {

    AvgAggregator aggregator = new AvgAggregator();


    @AfterEach
    void reset() {
        aggregator.reset();
    }

    @Test
    void addTest_argIsNotNumber_throwException() {
        // preparing
        String arg = "argument";

        // assert
        assertThrows(AggregatorsException.class, () -> aggregator.add(arg));
    }

    @Test
    void addTest_passCorrectValue_updateState() {
        // preparing
        Number value = 777.0;

        // act
        aggregator.add(value);

        // assert
        assertEquals(value, aggregator.getSum(), "Sum was not added.");
        assertEquals(1, aggregator.getCount(), "Count was not incremented.");
    }

    @Test
    void combineTest_argIsNotAvgAggregator_throwException() {
        // preparing
        MaxAggregator maxAggregator = new MaxAggregator();

        // assert
        assertThrows(AggregatorsException.class, () -> aggregator.combine(maxAggregator), "Must throw if provided aggregator is not identic to current.");
    }

    @Test
    void combineTest_passCorrectAggregator_mergeStates() {
        // preparing
        AvgAggregator secondAggregator = new AvgAggregator();
        secondAggregator.add(777);

        aggregator.add(777);

        // assert
        assertDoesNotThrow(() -> aggregator.combine(secondAggregator));
        assertEquals(1554.0, aggregator.getSum(), "Sum was not correctly update after combine.");
        assertEquals(2, aggregator.getCount(), "Count was not correctly update after combine.");
    }

    @Test
    void finishTest_countIsZero_throwException() {
        // assert
        assertThrows(AggregatorsException.class, () -> aggregator.finish(), "Must throw if denominator is zero.");   
    }

    @Test
    void finishTest_call_returnCorrectAverage() {
        // preparing
        aggregator.add(2);
        aggregator.add(2);

        // act
        Number result = aggregator.finish();

        // assert
        assertEquals(2.0, result.intValue());
    }

    @Test
    void resetTest_happyPath() {
        // preparing
        aggregator.add(4);

        // act
        aggregator.reset();

        // assert
        assertEquals(0.0, aggregator.getSum(), "Sum must be zero after reset.");
        assertEquals(0L, aggregator.getCount(), "Count must be zero after reset.");
    }
}
