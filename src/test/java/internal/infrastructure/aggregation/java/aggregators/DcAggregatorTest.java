package internal.infrastructure.aggregation.java.aggregators;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import com.volgoblob.internal.infrastructure.aggregation.java.aggregators.DcAggregator;
import com.volgoblob.internal.infrastructure.aggregation.java.aggregators.MaxAggregator;
import com.volgoblob.internal.infrastructure.aggregation.java.errors.AggregatorsException;

public class DcAggregatorTest {
    
    DcAggregator aggregator = new DcAggregator();

    @AfterEach
    void reset() {
        aggregator.reset();
    }

    @Test
    void addTest_argIsNotString_throwException() {
        // preparing
        int arg = 777;

        // assert
        assertThrows(AggregatorsException.class, () -> aggregator.add(arg), "Arg must be string");
    }

    @Test
    void addTest_passCorrectArg_addToSet() {
        // preparing
        String arg = "argument";

        // act
        aggregator.add(arg);

        //assert
        assertTrue(aggregator.getSet().contains(arg));
    }

    @Test
    void combineTest_argIsNull_throwException() {
        // assert
        assertThrows(AggregatorsException.class, () -> aggregator.combine(null), "Passed arg is not match to current class.");
    }
    
    @Test
    void combineTest_argIsNotDcAggregator_throwException() {
        // preparing
        MaxAggregator maxAggregator = new MaxAggregator();

        // assert
        assertThrows(AggregatorsException.class, () -> aggregator.combine(maxAggregator), "Passed arg is not match to current class.");
    }

    @Test
    void combineTest_correctArg_mergeSet() {
        // preparing
        String arg1 = "argument1";
        String arg2 = "argument2";

        DcAggregator secondAggregator = new DcAggregator();
        secondAggregator.add(arg1);

        aggregator.add(arg2);

        // assert
        assertDoesNotThrow(() -> aggregator.combine(secondAggregator));

        Set<String> resultSet = aggregator.getSet();

        assertEquals(2, resultSet.size(), "Some arg is not added to set after combine");
        assertTrue(resultSet.contains(arg1), "Do not added correct value after combine.");
        assertTrue(resultSet.contains(arg2), "Do not contain argument after combine.");
    }

    @Test
    void finishTest_call_returnCorrectResult() {
        // preparing
        aggregator.add("arg1");
        aggregator.add("arg2");

        // act
        Number result = aggregator.finish();

        // assert
        assertEquals(2, result, "Return incorrect result");
    }

    @Test
    void resetTest_call_truncateSet() {
        // preparing
        aggregator.add("arg1");

        // act
        aggregator.reset();

        // assert
        assertEquals(0, aggregator.getSet().size());
    }
}
