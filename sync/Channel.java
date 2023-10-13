package sync;

import java.util.Iterator;

/**
 * A abstart class that is the superclass os all channels.
 * This class is used for communicating between threads.
 * 
 * @param <T> The type that is sending between threads.
 * @see UnlimitedChannel
 * @see LimitedChannel
 * @see ReadOnlyChannel
 * @see WriteOnlyChannel
 */
public abstract class Channel<T> implements Iterator<T>, Iterable<T> {

    public static final class RuntimeInterruptedException extends RuntimeException {
        public final InterruptedException err;

        public RuntimeInterruptedException(InterruptedException e) {
            err = e;
        }
    }

    /**
     * Receive data from this channel that have been sended. 
     * This method will block until it receive data.
     * 
     * @return The received data.
     * @throws RuntimeInterruptedException
     */
    public abstract T recv();

    /**
     * Send data to this channel.
     * This method will block if the channel is full.
     * 
     * @param obj The sent data.
     * @throws RuntimeInterruptedException
     */
    public abstract void send(final T obj);

    /**
     * Create a read only channel.
     * This method will not copy the channel.
     * 
     * @return The read only channel.
     */
    public final ReadOnlyChannel<T> toReadOnlyChannel() {
        return new ReadOnlyChannel<>(this);
    }

    /**
     * Create a write only channel.
     * This method will not copy the channel.
     * 
     * @return The write only channel.
     */
    public final WriteOnlyChannel<T> toWriteOnlyChannel() {
        return new WriteOnlyChannel<>(this);
    }

    @Override
    public final boolean hasNext() {
        return true;
    }

    @Override
    public final T next() {
        return recv();
    }

    @Override
    public final Iterator<T> iterator() {
        return this;
    }
}
