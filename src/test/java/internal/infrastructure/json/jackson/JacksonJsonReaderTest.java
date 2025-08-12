package internal.infrastructure.json.jackson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.volgoblob.internal.domain.interfaces.aggregations.AggType;
import com.volgoblob.internal.domain.interfaces.aggregations.Aggregator;
import com.volgoblob.internal.domain.interfaces.parsers.JsonReader;
import com.volgoblob.internal.infrastructure.json.jackson.JacksonJsonReader;

@ExtendWith(MockitoExtension.class)
public class JacksonJsonReaderTest {

    @Mock
    Aggregator aggregator;

    @Captor
    ArgumentCaptor<Object> valueCaptor;

    @Captor
    ArgumentCaptor<Aggregator> aggregatorCaptor;

    @Test
    void readNoGroupTest_happyPath() throws URISyntaxException {
        // preparing
        JsonReader reader = new JacksonJsonReader();
        when(aggregator.finish()).thenReturn(777);
        Supplier<Aggregator> supplier = () -> aggregator;
        Path filePath = Paths.get(getClass().getResource("/TestData.json").toURI());
        

        // act
        Number result = reader.readNoGroup(filePath, AggType.AVG.name(), "age", supplier);

        // assert
        assertEquals(777, result);
        verify(aggregator, times(2)).add(valueCaptor.capture());
        verify(aggregator, times(1)).combine(aggregatorCaptor.capture());
    }
}
