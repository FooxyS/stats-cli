package com.volgoblob.internal.usecase;

import com.volgoblob.internal.domain.interfaces.AggregationFunctions;
import com.volgoblob.internal.domain.interfaces.JsonParser;
import com.volgoblob.internal.domain.interfaces.Profiler;

/**
 * AggregateJsonUseCase is the main usecase of this application.
 */
public class AggregateJsonUseCase {
    private final JsonParser jsonParser;
    private final AggregationFunctions aggregationFunctions;
    private final Profiler profiler;

    public AggregateJsonUseCase(JsonParser jsonParser, AggregationFunctions aggregationFunctions, Profiler profiler) {
        this.jsonParser = jsonParser;
        this.aggregationFunctions = aggregationFunctions;
        this.profiler = profiler;
    }

    /**
     * Execute method is used to run the business logic of this usecase.
     */
    public void execute(String aggregationName, String fieldName, String groupFields, String jsonFile) {
        // TODO: add business logic of this usecase

        // парсим json, получаем map, выводим бенчмарк. Мапа вида: Map ["logstash", "timestamp", "status"] -> [3, 5, 6]

        // проходимся по мапе, выполняем агрегацию, собираем результат, выводим бенчмарк
    }
}
