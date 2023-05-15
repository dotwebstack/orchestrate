package org.dotwebstack.orchestrate.model.mappers;

import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.orchestrate.model.ModelException;
import org.projectnessie.cel.checker.Decls;
import org.projectnessie.cel.tools.Script;
import org.projectnessie.cel.tools.ScriptCreateException;
import org.projectnessie.cel.tools.ScriptException;
import org.projectnessie.cel.tools.ScriptHost;

public class CelType implements ResultMapperType {

  @Override
  public String getName() {
    return "cel";
  }

  @Override
  public ResultMapper create(Map<String, Object> options) {
    var scriptHost = ScriptHost.newBuilder()
        .build();

    var expr = (String) options.get("expr");
    Script script;

    try {
      script = scriptHost.buildScript(expr)
          .withDeclarations(Decls.newVar("result", Decls.Any))
          .build();
    } catch (ScriptCreateException e) {
      throw new ModelException("Could not parse expression: " + expr, e);
    }

    return result -> {
      var arguments = new HashMap<String, Object>();
      arguments.put("result", result);

      try {
        return script.execute(Object.class, arguments);
      } catch (ScriptException e) {
        throw new ModelException("Could not evaluate expression: " + expr, e);
      }
    };
  }
}