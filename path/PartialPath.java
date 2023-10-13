package path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface PartialPath {

    Object match(final String str);

    public static record PartialPathsWithParams(List<PartialPath> paths, List<String> params) {}

    public static PartialPathsWithParams parse(final String path) {
        ArrayList<String> params = new ArrayList<>();
        List<PartialPath> paths = Arrays
        .stream(path.split("/"))
        .filter((final String s) -> !s.isBlank())
        .map((String partialPath) -> {
            if (partialPath.startsWith("<") && partialPath.endsWith(">")) {
                String[] content = partialPath.substring(1, partialPath.length()-1).split(":");
                String pathType = content[0];
                String pathName = content[1];
                params.add(pathName);
                return switch (pathType) {
                    case "int" -> new IntPath();
                    case "any" -> new AnyPath();
                    default -> null; // TODO: Handle the default case
                };
            }
            return new FixedPath(partialPath);
        })
        .collect(Collectors.toList());
        return new PartialPathsWithParams(paths, params);
    }
}
