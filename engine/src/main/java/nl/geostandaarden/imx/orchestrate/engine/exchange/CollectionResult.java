package nl.geostandaarden.imx.orchestrate.engine.exchange;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;

@Getter
@Builder(toBuilder = true)
public class CollectionResult implements DataResult {

    private final ObjectType type;

    @Singular
    private final List<ObjectResult> objectResults;

    public List<Map<String, Object>> getPropertyList() {
        return objectResults.stream() //
                .map(ObjectResult::getProperties)
                .toList();
    }
}
