package sync;

import java.util.Stack;

public final class UnlimitedChannel<T> extends Channel<T> {

    private final Stack<T> list;

    public UnlimitedChannel() {
        list = new Stack<>();
    }

    @Override
    public T recv() {
        synchronized (list) {
            try {
                list.wait();
            } catch (InterruptedException e) {
                throw new RuntimeInterruptedException(e);
            }
            return list.pop();
        }
    }

    @Override
    public void send(final T obj) {
        synchronized (list) {
            list.add(obj);
            list.notify();
        }
    }
    
}
