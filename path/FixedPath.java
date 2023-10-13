package path;

public record FixedPath(String path) implements PartialPath {

    @Override
    public Object match(final String str) {
        if (str.equals(path)) {
            return new Null();
        }
        return null;
    }

}
