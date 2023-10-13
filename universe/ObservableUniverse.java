package universe;

import java.util.Objects;

import sync.WriteOnlyChannel;

public final class ObservableUniverse {
    
    private final WriteOnlyChannel<Object> ch;

    public ObservableUniverse(final Universe.Lock lock, final WriteOnlyChannel<Object> channel) throws NullPointerException {
        Objects.requireNonNull(lock);
        ch = channel;
    }

    public void shutdown() {
        ch.send(new ShutdownException("Shutdown method get executed."));
    }

}
