package org.dotwebstack.orchestrate.model;

import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.dotwebstack.orchestrate.model.filters.Filter;

@Getter
@Jacksonized
@Builder(toBuilder = true)
public final class SourceRelation {

  private final ObjectTypeRef sourceType;

  private final Relation property;

  private final Set<Filter> filters;
}
