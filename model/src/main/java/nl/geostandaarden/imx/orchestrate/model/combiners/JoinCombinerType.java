package nl.geostandaarden.imx.orchestrate.model.combiners;

import java.util.Map;
import java.util.stream.Collectors;
import nl.geostandaarden.imx.orchestrate.model.PathResult;
import nl.geostandaarden.imx.orchestrate.model.PropertyResult;

public final class JoinCombinerType implements ResultCombinerType {

  @Override
  public String getName() {
    return "join";
  }

  @Override
  public ResultCombiner create(Map<String, Object> options) {
    return pathResults -> {
      var nonEmptyResults = pathResults
          .stream()
          .filter(PathResult::isNotNull)
          .toList();

      if (nonEmptyResults.isEmpty()) {
        return PropertyResult.empty();
      }

      var value = nonEmptyResults.stream()
          .map(PathResult::getValue)
          .map(String::valueOf)
          .collect(Collectors.joining());

      var sourceProperties = nonEmptyResults.stream()
          .map(PathResult::getSourceProperty)
          .collect(Collectors.toSet());

      return PropertyResult.builder()
          .value(value)
          .sourceProperties(sourceProperties)
          .build();
    };
  }
}
