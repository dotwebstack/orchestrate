package nl.geostandaarden.imx.orchestrate.engine.fetch.stage;

import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionResult;
import nl.geostandaarden.imx.orchestrate.engine.exchange.DataResult;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectResult;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

public final class StageExecutor {

    public Mono<DataResult> execute(Stage stage) {
        var request = stage.getRequest();

        if (request instanceof ObjectRequest objectRequest) {
            return execute(stage, objectRequest);
        }

        if (request instanceof CollectionRequest collectionRequest) {
            return execute(stage, collectionRequest);
        }

        throw new OrchestrateException("Could not execute request: " + request.getClass());
    }

    private Mono<DataResult> execute(Stage stage, ObjectRequest request) {
        var selection = request.getSelection();
        var repository = selection.getSource().getDataRepository();

        return repository
                .findOne(request) //
                .flatMap(properties -> {
                    var result = ObjectResult.builder()
                            .type(selection.getObjectType())
                            .properties(properties)
                            .build();

                    return stage.getNextStages(result)
                            .flatMap(nextStage -> execute(nextStage)
                                    .map(nextResult -> Tuples.of(nextResult, nextStage.getNextResultCombiner())))
                            .reduce(result, (accResult, nextResultTuple) -> {
                                var nextResultCombiner = nextResultTuple.getT2();
                                return nextResultCombiner.combine(accResult, nextResultTuple.getT1());
                            });
                });
    }

    private Mono<DataResult> execute(Stage stage, CollectionRequest request) {
        var selection = request.getSelection();
        var repository = selection.getSource().getDataRepository();

        return repository
                .find(request)
                .map(properties -> ObjectResult.builder()
                        .type(selection.getObjectType())
                        .properties(properties)
                        .build())
                .collectList()
                .flatMap(objectResults -> {
                    var result = CollectionResult.builder()
                            .type(selection.getObjectType())
                            .objectResults(objectResults)
                            .build();

                    return stage.getNextStages(result) //
                            .flatMap(this::execute)
                            .reduce(result, stage.getNextResultCombiner()::combine);
                });
    }
}
