package universe;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import procotol.Status;

/**
 * Class that is used for building Universe.
 * 
 * @see Universe
 */
public class UniverseBuilder {

    private int port;
    private String addr;
    private ExecutorService executor;
    private boolean shutdownWhenEncounterError;
    private InputStream commandStream;
    private Status defaultStatus;

    public UniverseBuilder() {
        port = 8080;
        addr = "127.0.0.1";
        executor = Executors.newVirtualThreadPerTaskExecutor();
        shutdownWhenEncounterError = true;
        commandStream = System.in;
        defaultStatus = Status.NOT_FOUND;
    }

    public UniverseBuilder setPort(final int port) {
        this.port = port;
        return this;
    }

    public UniverseBuilder setAddress(final String addr) {
        this.addr = addr;
        return this;
    }

    public UniverseBuilder setExecutor(final ExecutorService exec) {
        this.executor = exec;
        return this;
    }

    public UniverseBuilder setShutdownWhenEncounterError(final boolean config) {
        shutdownWhenEncounterError = config;
        return this;
    }

    public UniverseBuilder setCommandListener(final InputStream commandListener) {
        this.commandStream = commandListener;
        return this;
    }

    public UniverseBuilder setDefaultStatus(final Status defaultStatus) {
        this.defaultStatus = defaultStatus;
        return this;
    }

    /**
     * Build the Universe.
     * 
     * @return The built Universe.
     * @throws IOException
     * @see Universe
     */
    public Universe build() throws IOException {
        return new Universe(addr, port, executor, shutdownWhenEncounterError, commandStream, defaultStatus);
    }
    
}
