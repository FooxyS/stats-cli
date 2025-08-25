package internal.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.volgoblob.internal.domain.interfaces.aggregations.Aggregator;
import com.volgoblob.internal.domain.interfaces.aggregations.AggregatorForGroup;
import com.volgoblob.internal.domain.interfaces.aggregations.AggregatorsRegistry;
import com.volgoblob.internal.domain.interfaces.parsers.JsonReader;
import com.volgoblob.internal.domain.interfaces.parsers.JsonWriter;
import com.volgoblob.internal.domain.interfaces.parsers.ParsersAdapter;
import com.volgoblob.internal.infrastructure.aggregation.java.aggregators.AvgAggregator;
import com.volgoblob.internal.infrastructure.aggregation.java.aggregators.DcAggregator;
import com.volgoblob.internal.infrastructure.aggregation.java.aggregators.MaxAggregator;
import com.volgoblob.internal.infrastructure.aggregation.java.errors.AggregatorsException;
import com.volgoblob.internal.infrastructure.aggregation.nativeGo.aggregators.NativeDc;
import com.volgoblob.internal.usecase.AggregateJsonUseCase;

@ExtendWith(MockitoExtension.class)
public class AggregateJsonUseCaseTest {
    
    @Mock
    private JsonReader jsonReader;

    @Mock
    private JsonWriter jsonWriter;

    @Mock
    private ParsersAdapter parsersAdapter;

    @Mock
    private AggregatorsRegistry aggregatorsRegistry;

    @Mock
    private AggregatorForGroup groupAggregator;

    public static Stream<Arguments> invalidArgProvider() {
        String aggregationName = "someAgg";
        String fieldName = "someField";
        Path jsonPath = Path.of("somePath");
        return Stream.of(
            Arguments.of(null, fieldName, null, jsonPath, "aggregationName"),
            Arguments.of(aggregationName, null, null, jsonPath, "fieldName"),
            Arguments.of(aggregationName, fieldName, null, null, "jsonPath"),
            Arguments.of("  ", fieldName, null, jsonPath, "aggregationName"),
            Arguments.of(aggregationName, "   ", null, jsonPath, "fieldName")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidArgProvider")
    void executeTest_invalidInput_throws(String aggregationName, String fieldName, List<String> groupFields, Path jsonPath, String name) {
        // preparing
        AggregateJsonUseCase aggregateJsonUseCase = new AggregateJsonUseCase(parsersAdapter, aggregatorsRegistry, groupAggregator, false);

        // assert
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> aggregateJsonUseCase.execute(aggregationName, fieldName, groupFields, jsonPath), "Usecase did not throw exception when args is null.");
        assertTrue(e.getMessage().contains(name), "Error message do not contain name of invalid arg.");
    }

    @Test
    void executeTest_registryNull_throws(@TempDir Path tempdir) {
        // preparing
        AggregateJsonUseCase aggregateJsonUseCase = new AggregateJsonUseCase(parsersAdapter, aggregatorsRegistry, groupAggregator, false);
        
        String invaidAgg = "ABOBA";
        String fieldName = "number";
        Path jsonPath = tempdir.resolve("input.json");

        // mock
        when(aggregatorsRegistry.create(invaidAgg)).thenReturn(null);

        // assert
        assertThrows(AggregatorsException.class, () -> aggregateJsonUseCase.execute(invaidAgg, fieldName, null, jsonPath), "Usecase did not throw exception when registry return null.");

    }

    // aggregatorsRegistry.create
    @Test
    void executeTest_groupAggregation_aggGroupUseCase(@TempDir Path tempdir) {
        // preparing
        AggregateJsonUseCase aggregateJsonUseCase = new AggregateJsonUseCase(parsersAdapter, aggregatorsRegistry, groupAggregator, false);

        String aggregationName = "MAX";
        String fieldName = "number";
        List<String> groupFields = List.of("el1", "el2");
        Path jsonPath = tempdir.resolve("input.json");

        // mocks
        Map<List<Object>, Number> resultMap = new HashMap<>();
        resultMap.put(List.of("some val", "some val"), 777);
        when(jsonReader.readWithGroup(jsonPath, aggregationName, fieldName, groupAggregator)).thenReturn(resultMap);
        when(parsersAdapter.getJsonReader()).thenReturn(jsonReader);

        String pathToResultFile = "Path to result file";
        when(jsonWriter.writeResultToJson(aggregationName, groupFields, fieldName, resultMap)).thenReturn(pathToResultFile);
        when(parsersAdapter.getJsonWriter()).thenReturn(jsonWriter);

        // act
        String result = aggregateJsonUseCase.execute(aggregationName.toLowerCase(), fieldName, groupFields, jsonPath);

        // assert
        verify(groupAggregator, times(1)).register(groupFields);
        verify(jsonReader, times(1)).readWithGroup(jsonPath, aggregationName, fieldName, groupAggregator);
        verify(parsersAdapter, times(1)).getJsonReader();
        verify(jsonWriter, times(1)).writeResultToJson(aggregationName, groupFields, fieldName, resultMap);
        verify(parsersAdapter, times(1)).getJsonWriter();

        assertEquals("Creating is successful. Path to your file: " + pathToResultFile, result, "Result string did not contain path to created json file.");
    }

    @Test
    void executeTest_inputForNative_nativeAggProccess(@TempDir Path tempdir) {
        // preparing
        AggregateJsonUseCase aggregateJsonUseCase = new AggregateJsonUseCase(parsersAdapter, aggregatorsRegistry, groupAggregator, true);

        String aggregationName = "DC";
        String fieldName = "number";
        Path jsonPath = tempdir.resolve("input.json");

        Supplier<Aggregator> supplier = NativeDc::new;

        // mocks
        when(aggregatorsRegistry.create(aggregationName)).thenReturn(supplier);

        Number expectedResult = 4;
        when(jsonReader.readNoGroup(jsonPath, aggregationName, fieldName, supplier)).thenReturn(expectedResult);

        when(parsersAdapter.getNativeJsonReader()).thenReturn(jsonReader);

        // act
        String result = aggregateJsonUseCase.execute(aggregationName.toLowerCase(), fieldName, null, jsonPath);

        // assert
        verify(aggregatorsRegistry, times(1)).create(aggregationName);
        verify(parsersAdapter, times(1)).getNativeJsonReader();
        verify(jsonReader, times(1)).readNoGroup(jsonPath, aggregationName, fieldName, supplier);

        assertTrue(result.contains(expectedResult.toString()));
        assertTrue(result.contains(fieldName));
        assertTrue(result.contains(aggregationName));
        assertTrue(result.contains(jsonPath.toString()));
    }


    public static Stream<Arguments> aggregatorsInputProvider() {
        return Stream.of(
            Arguments.of("DC", (Supplier<Aggregator>) DcAggregator::new),
            Arguments.of("MAX", (Supplier<Aggregator>) MaxAggregator::new),
            Arguments.of("AVG", (Supplier<Aggregator>) AvgAggregator::new)
        );
    }

    @ParameterizedTest
    @MethodSource("aggregatorsInputProvider")
    void executeTest_inputForDefault_defaultAggProccess(String aggregationName, Supplier<Aggregator> supplier, @TempDir Path tempdir) {
        // preparing
        AggregateJsonUseCase aggregateJsonUseCase = new AggregateJsonUseCase(parsersAdapter, aggregatorsRegistry, groupAggregator, false);

        String fieldName = "number";
        Path jsonPath = tempdir.resolve("input.json");

        // mock
        when(aggregatorsRegistry.create(aggregationName)).thenReturn(supplier);
        when(parsersAdapter.getJsonReader()).thenReturn(jsonReader);

        Number expectedAggResult = 777;
        when(jsonReader.readNoGroup(jsonPath, aggregationName, fieldName, supplier)).thenReturn(expectedAggResult);

        // act
        String result = aggregateJsonUseCase.execute(aggregationName.toLowerCase(), fieldName, null, jsonPath);

        // assert
        verify(aggregatorsRegistry, times(1)).create(aggregationName);
        verify(parsersAdapter, times(1)).getJsonReader();
        verify(jsonReader, times(1)).readNoGroup(jsonPath, aggregationName, fieldName, supplier);

        assertTrue(result.contains(expectedAggResult.toString()), "Result string did not contain result number");
        assertTrue(result.contains(fieldName), "Result string did not contain json field for agg");
        assertTrue(result.contains(aggregationName), "Result string did not contain agg name");
        assertTrue(result.contains(jsonPath.toString()), "Result string did not contain path to input file");
    }

}
