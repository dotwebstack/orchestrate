package org.dotwebstack.orchestrate.source;

import static java.util.Collections.emptyList;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.orchestrate.model.Property;

@Getter
@RequiredArgsConstructor
public final class SelectedProperty {

  private final Property property;

  private final List<SelectedProperty> selectedProperties;

  public SelectedProperty(Property property) {
    this(property, emptyList());
  }
}
