package org.dotwebstack.orchestrate.model;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(toBuilder = true)
abstract class AbstractProperty implements Property {

  protected final String name;

  protected final boolean identifier;

  @Builder.Default
  protected final Cardinality cardinality = Cardinality.OPTIONAL;
}
