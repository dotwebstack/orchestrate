package nl.geostandaarden.imx.orchestrate.source.graphql.mapper;

import graphql.ExecutionInput;
import graphql.language.Field;
import graphql.language.SelectionSet;
import java.util.List;
import java.util.Set;
import nl.geostandaarden.imx.orchestrate.engine.exchange.DataRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.SelectedProperty;
import nl.geostandaarden.imx.orchestrate.engine.source.SourceException;

abstract class AbstractGraphQlMapper<T extends DataRequest> {

  abstract ExecutionInput convert(T request);

  protected SelectionSet createSelectionSet(Set<SelectedProperty> selectedProperties) {
    if (selectedProperties.isEmpty()) {
      throw new SourceException("SelectionSet cannot be empty.");
    }

    var fields = selectedProperties.stream()
        .map(this::getField)
        .toList();

    return new SelectionSet(fields);
  }

  private Field getField(SelectedProperty property) {
    SelectionSet selectionSet = null;

    if (property.getNestedRequest() != null) {
      selectionSet = createSelectionSet(property.getNestedRequest()
          .getSelectedProperties());
    }

    return new Field(property.getProperty()
        .getName(), List.of(), selectionSet);
  }
}
