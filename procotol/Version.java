package procotol;

/**
 * A enum for HTTP Version. Support HTTP/0.9, HTTP/1.0, HTTP/1.1, HTTP/2.
 */
public enum Version {
    ZERO_POINT_NINE(0.9f),
    ONE(1),
    ONE_POINT_ONE(1.1f),
    TWO(2);

    public final float version;

    private Version(final float ver) {
        version = ver;
    }

    /**
     * Parse HTTP version.
     * 
     * @param httpVersion Version for parsing.
     * @return The corresponding version unless null.
     */
    public static Version parse(final String httpVersion) {
        return switch (httpVersion) {
            case "HTTP/0.9" -> ZERO_POINT_NINE;
            case "HTTP/1.0" -> ONE;
            case "HTTP/1.1" -> ONE_POINT_ONE;
            case "HTTP/2" -> TWO;
            default -> null;
        };
    }

    @Override
    public String toString() {
        return switch (this) {
            case ZERO_POINT_NINE -> "HTTP/0.9";
            case ONE -> "HTTP/1.0";
            case ONE_POINT_ONE -> "HTTP/1.1";
            case TWO -> "HTTP/2";
        };
    }
}
