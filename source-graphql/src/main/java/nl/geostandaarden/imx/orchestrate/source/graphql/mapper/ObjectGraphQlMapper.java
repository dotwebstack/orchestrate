package nl.geostandaarden.imx.orchestrate.source.graphql.mapper;

import static org.springframework.util.StringUtils.uncapitalize;

import graphql.ExecutionInput;
import graphql.language.Argument;
import graphql.language.AstPrinter;
import graphql.language.Field;
import graphql.language.OperationDefinition;
import graphql.language.SelectionSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.source.graphql.config.GraphQlOrchestrateConfig;

@RequiredArgsConstructor
public class ObjectGraphQlMapper extends AbstractGraphQlMapper<ObjectRequest> {

  private static final String OPERATION_NAME = "Query";

  private final GraphQlOrchestrateConfig config;

  @Override
  public ExecutionInput convert(ObjectRequest request) {
    var fieldName = uncapitalize(request.getObjectType()
        .getName());

    var arguments = getArguments(request);

    var selectionSet = createSelectionSet(request.getSelectedProperties());
    var queryField = new Field(fieldName, arguments, selectionSet);

    var query = OperationDefinition.newOperationDefinition()
        .name(OPERATION_NAME)
        .operation(OperationDefinition.Operation.QUERY)
        .selectionSet(new SelectionSet(List.of(queryField)))
        .build();

    return ExecutionInput.newExecutionInput()
        .query(AstPrinter.printAst(query))
        .build();
  }


  private List<Argument> getArguments(ObjectRequest request) {
    return request.getObjectKey()
        .entrySet()
        .stream()
        .map(entry -> getArgument(entry.getKey(), entry.getValue()))
        .toList();
  }
  private Argument getArgument(String name, Object value) {
    return new Argument(name, ValueMapper.mapToValue(value));
  }

}
