package nl.geostandaarden.imx.orchestrate.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Singular;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Path {

    private static final String PATH_SEPARATOR = "/";

    @Singular
    private final List<String> segments;

    public String getFirstSegment() {
        return segments.get(0);
    }

    public String getLastSegment() {
        return segments.get(segments.size() - 1);
    }

    public boolean isLeaf() {
        return segments.size() == 1;
    }

    public Path withoutFirstSegment() {
        return new Path(segments.subList(1, segments.size()));
    }

    public Path append(Path path) {
        return new Path(
                Stream.concat(segments.stream(), path.getSegments().stream()).toList());
    }

    public static Path fromString(String path) {
        var segments = Arrays.asList(path.split(PATH_SEPARATOR));
        return new Path(segments);
    }

    public static Path fromProperties(Property... properties) {
        var segments = Arrays.stream(properties).map(Property::getName).toList();

        return new Path(segments);
    }

    @Override
    public String toString() {
        return String.join(PATH_SEPARATOR, segments);
    }
}
