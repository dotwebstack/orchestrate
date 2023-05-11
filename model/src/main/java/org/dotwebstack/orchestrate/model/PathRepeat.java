package org.dotwebstack.orchestrate.model;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.dotwebstack.orchestrate.model.matchers.ResultMatcher;

@Getter
@Jacksonized
@Builder(toBuilder = true)
public final class PathRepeat {

  private final ResultMatcher untilMatch;
}
