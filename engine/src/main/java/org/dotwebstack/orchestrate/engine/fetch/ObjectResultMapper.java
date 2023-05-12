package org.dotwebstack.orchestrate.engine.fetch;

import static java.util.Collections.unmodifiableMap;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.cast;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.noopCombiner;
import static org.dotwebstack.orchestrate.engine.schema.SchemaConstants.HAS_LINEAGE_FIELD;

import graphql.schema.DataFetchingFieldSelectionSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import lombok.Builder;
import org.dotwebstack.orchestrate.engine.OrchestrateException;
import org.dotwebstack.orchestrate.model.AbstractRelation;
import org.dotwebstack.orchestrate.model.Attribute;
import org.dotwebstack.orchestrate.model.ModelMapping;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.model.Path;
import org.dotwebstack.orchestrate.model.PathMapping;
import org.dotwebstack.orchestrate.model.PathResult;
import org.dotwebstack.orchestrate.model.Property;
import org.dotwebstack.orchestrate.model.PropertyMapping;
import org.dotwebstack.orchestrate.model.PropertyResult;
import org.dotwebstack.orchestrate.model.combiners.CoalesceType;
import org.dotwebstack.orchestrate.model.combiners.NoopType;
import org.dotwebstack.orchestrate.model.lineage.ObjectLineage;
import org.dotwebstack.orchestrate.model.lineage.ObjectReference;
import org.dotwebstack.orchestrate.model.lineage.OrchestratedProperty;
import org.dotwebstack.orchestrate.model.lineage.PropertyMappingExecution;
import org.dotwebstack.orchestrate.model.lineage.SourceProperty;

@Builder
public final class ObjectResultMapper {

  private final ModelMapping modelMapping;

  public Map<String, Object> map(ObjectResult objectResult, ObjectType targetType,
      DataFetchingFieldSelectionSet selectionSet) {
    var targetMapping = modelMapping.getObjectTypeMapping(targetType);
    var resultMap = new HashMap<String, Object>();
    var lineageBuilder = ObjectLineage.builder();

    selectionSet.getImmediateFields()
        .forEach(field -> {
          if (!targetType.hasProperty(field.getName())) {
            return;
          }

          var property = targetType.getProperty(field.getName());
          var propertyResult = mapProperty(objectResult, property, targetMapping.getPropertyMapping(property));

          if (propertyResult == null) {
            return;
          }

          Object propertyValue = null;

          if (property instanceof Attribute attribute) {
            propertyValue = mapAttribute(attribute, propertyResult.getValue());
          }

          if (property instanceof AbstractRelation relation) {
            propertyValue = mapRelation(relation, propertyResult.getValue(), field.getSelectionSet());
          }

          if (propertyValue != null) {
            resultMap.put(property.getName(), propertyValue);

            lineageBuilder.orchestratedProperty(OrchestratedProperty.builder()
                .subject(ObjectReference.builder()
                    .objectType(targetType.getName())
                    .objectKey(objectResult.getKey())
                    .build())
                .property(property.getName())
                .value(propertyValue)
                .isDerivedFrom(propertyResult.getSourceProperties())
                .wasGeneratedBy(PropertyMappingExecution.builder()
                    .used(org.dotwebstack.orchestrate.model.lineage.PropertyMapping.builder()
                        .pathMapping(Set.of())
                        .build())
                    .build())
                .build());
          }
        });

    resultMap.put(HAS_LINEAGE_FIELD, lineageBuilder.build());
    return unmodifiableMap(resultMap);
  }

  private Object mapAttribute(Attribute attribute, Object value) {
    return attribute.getType()
        .mapSourceValue(value);
  }

  private Object mapRelation(AbstractRelation relation, Object value, DataFetchingFieldSelectionSet selectionSet) {
    var relType = modelMapping.getTargetType(relation.getTarget());

    if (value instanceof ObjectResult nestedObjectResult) {
      return map(nestedObjectResult, relType, selectionSet);
    }

    if (value instanceof List<?> nestedObjectResultList) {
      return nestedObjectResultList.stream()
          .map(ObjectResult.class::cast)
          .map(nestedObjectResult -> map(nestedObjectResult, relType, selectionSet))
          .toList();
    }

    return null;
  }

  private PropertyResult mapProperty(ObjectResult objectResult, Property property, PropertyMapping propertyMapping) {
    var pathResults = propertyMapping.getPathMappings()
        .stream()
        .flatMap(pathMapping -> pathResult(objectResult, pathMapping))
        .toList();

    var combiner = propertyMapping.getCombiner();

    if (combiner == null) {
      var hasMultiCardinality = !property.getCardinality()
          .isSingular();

      combiner = hasMultiCardinality
          ? new NoopType().create(Map.of())
          : new CoalesceType().create(Map.of());
    }

    return combiner.apply(pathResults);
  }

  private Stream<PathResult> pathResult(ObjectResult objectResult, PathMapping pathMapping) {
    var pathResults = pathResult(objectResult, pathMapping.getPath());

    return pathResults.flatMap(pathResult -> {
      // TODO: Lazy fetching & multi cardinality
      var nextedPathResult = pathMapping.getNextPathMappings()
          .stream()
          .flatMap(nextPathMapping -> {
            var ifMatch = nextPathMapping.getIfMatch();

            if (ifMatch != null && ifMatch.test(pathResult.getValue())) {
              return pathResult(objectResult, nextPathMapping);
            }

            return Stream.of(pathResult);
          })
          .findFirst()
          .orElse(pathResult);

      var mappedValue = pathMapping.getResultMappers()
          .stream()
          .reduce(nextedPathResult.getValue(), (acc, resultMapper) -> resultMapper.apply(acc), noopCombiner());

      return Stream.of(nextedPathResult.withValue(mappedValue));
    });
  }

  private Stream<PathResult> pathResult(ObjectResult objectResult, Path path) {
    if (path.isLeaf()) {
      var propertyValue = objectResult.getProperty(path.getFirstSegment());

      var pathResult = PathResult.builder()
          .value(propertyValue)
          .sourceProperty(SourceProperty.builder()
              .subject(ObjectReference.builder()
                  .objectType(objectResult.getType().getName())
                  .objectKey(objectResult.getKey())
                  .build())
              .property(path.getFirstSegment())
              .value(propertyValue)
              .path(path.getSegments())
              .build())
          .build();

      return Stream.of(pathResult);
    }

    var nestedResult = objectResult.getProperty(path.getFirstSegment());

    if (nestedResult == null) {
      return Stream.of(PathResult.empty());
    }

    var remainingPath = path.withoutFirstSegment();

    if (nestedResult instanceof ObjectResult nestedObjectResult) {
      return pathResult(nestedObjectResult, remainingPath);
    }

    if (nestedResult instanceof CollectionResult nestedCollectionResult) {
      return nestedCollectionResult.getObjectResults()
          .stream()
          .flatMap(nestedObjectResult -> pathResult(nestedObjectResult, remainingPath));
    }

    if (nestedResult instanceof Map<?, ?> mapResult) {
      return pathResult(objectResult.withProperties(cast(mapResult)), remainingPath);
    }

    throw new OrchestrateException("Could not map path: " + path.toString());
  }
}
