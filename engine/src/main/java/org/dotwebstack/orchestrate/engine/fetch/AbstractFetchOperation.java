package org.dotwebstack.orchestrate.engine.fetch;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import lombok.Builder;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.source.SelectedProperty;
import org.dotwebstack.orchestrate.source.Source;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@SuperBuilder(toBuilder = true)
abstract class AbstractFetchOperation implements FetchOperation {

  protected final Source source;

  protected final ObjectType objectType;

  @Singular
  protected final List<SelectedProperty> selectedProperties;

  @Singular
  protected final Map<String, FetchOperation> nextOperations;

  @Builder.Default
  protected final UnaryOperator<Map<String, Object>> inputMapper = UnaryOperator.identity();

  protected Mono<ObjectResult> executeNextOperations(ObjectResult input) {
    if (nextOperations.isEmpty()) {
      return Mono.just(input);
    }

    return Flux.fromIterable(nextOperations.entrySet())
        .flatMap(entry -> {
          var nextOperation = entry.getValue();
          return Mono.from(nextOperation.execute(input.getProperties()))
              .map(nestedResult -> Tuples.of(entry.getKey(), nestedResult));
        })
        .collect(input::toBuilder, (builder, tuple) -> builder.nestedObject(tuple.getT1(), tuple.getT2()))
        .map(ObjectResult.ObjectResultBuilder::build);
  }
}
