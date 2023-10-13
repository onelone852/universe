package procotol;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Objects;

public final class ResponseStream extends PrintStream {

    private Status status;
    private Version version = Version.ONE_POINT_ONE;

    private static class StringBuilderOutputStream extends OutputStream {
        private StringBuilder res;

        public StringBuilderOutputStream(final StringBuilder builder) {
            res = builder;
        }

        @Override
        public void write(int b) throws IOException {
            res.append((char) b);
        }

    }

    public ResponseStream(final universe.Universe.Lock lock, final StringBuilder res, final Status status) throws NullPointerException {
        super(new StringBuilderOutputStream(res), false);
        Objects.requireNonNull(lock);
        this.status = status;
    }

    /**
     * Set the status of this response.
     * 
     * @param status Status that is set.
     */
    public void setStatus(final Status status) {
        this.status = status;
    }

    /**
     * Get the status of this response.
     * 
     * @return Status of this response.
     */
    public Status getStatus() {
        return this.status;
    }

    /**
     * Set the version of this response.
     * 
     * @param version Version that is set.
     */
    public void setVersion(final Version version) {
        this.version = version;
    }

    /**
     * Get the version of this response.
     * 
     * @return Version of this response.
     */
    public Version getVersion() {
        return this.version;
    }
}
