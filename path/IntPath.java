package path;

public record IntPath() implements PartialPath {

    @Override
    public Object match(final String str) {
        try {
            int i = Integer.parseInt(str);
            return i;
        } catch (NumberFormatException e) {
            return null;
        }
        
    }
    
}
