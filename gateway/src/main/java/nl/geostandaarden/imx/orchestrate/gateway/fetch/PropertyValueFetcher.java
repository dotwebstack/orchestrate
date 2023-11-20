package nl.geostandaarden.imx.orchestrate.gateway.fetch;

import java.util.Map;
import java.util.function.UnaryOperator;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import lombok.AllArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;

@AllArgsConstructor
public final class PropertyValueFetcher implements DataFetcher<Map<String, Object>> {

  private final UnaryOperator<String> fieldRenamer;

  @SuppressWarnings("unchecked")
  @Override
  public Map<String, Object> get(DataFetchingEnvironment environment) {
    var sourceMap = (Map<String, Object>) environment.getSource();
    var value = sourceMap.get(fieldRenamer.apply("value"));

    if (value == null) {
      return null;
    }

    if (value instanceof Integer) {
      return Map.of(fieldRenamer.apply("integerValue"), value);
    }

    if (value instanceof Boolean) {
      return Map.of(fieldRenamer.apply("booleanValue"), value);
    }

    if (value instanceof String) {
      return Map.of(fieldRenamer.apply("stringValue"), value);
    }

    // TODO: refactor (see issue #1)
    if (value instanceof Map) {
      return Map.of(fieldRenamer.apply("objectValue"), value);
    }

    throw new OrchestrateException("Could not map value: " + value.toString());
  }
}
