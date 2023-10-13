package sync;

import java.util.Iterator;

/**
 * Read only channel. 
 * 
 * @see Channel
 * @see WriteOnlyChannel
 * @param <T> Type that is sending between threads.
 */
public class ReadOnlyChannel<T> implements Iterable<T> {

    private final Channel<T> ch;

    public ReadOnlyChannel(final Channel<T> ch) {
        this.ch = ch;
    }

    /**
     * Receive data from this channel that have been sended. 
     * This method will block until it receive data.
     * 
     * @return The received data.
     * @throws RuntimeInterruptedException
     */
    public T recv() {
        return ch.recv();
    } 

    @Override
    public Iterator<T> iterator() {
        return ch;
    }
}


