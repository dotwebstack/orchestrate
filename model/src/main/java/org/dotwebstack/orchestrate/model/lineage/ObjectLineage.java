package org.dotwebstack.orchestrate.model.lineage;

import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder(toBuilder = true)
public final class ObjectLineage {

  @Singular
  private final Set<OrchestratedProperty> orchestratedProperties;
}
