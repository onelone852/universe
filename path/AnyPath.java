package path;

public record AnyPath() implements PartialPath {

    @Override
    public Object match(final String str) {
        return str;
    }
    
}
