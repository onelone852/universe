package sync;

/**
 * Write only channel. 
 * 
 * @see Channel
 * @see ReadOnlyChannel
 * @param <T> Type that is sending between threads.
 */
public class WriteOnlyChannel<T> {

    private final Channel<T> ch;

    public WriteOnlyChannel(final Channel<T> ch) {
        this.ch = ch;
    }

    /**
     * Send data to this channel.
     * This method will block if the channel is full.
     * 
     * @param obj The eended data.
     * @throws RuntimeInterruptedException
     */
    public void send(final T obj) {
        ch.send(obj);
    }

}
