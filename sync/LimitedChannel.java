package sync;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public final class LimitedChannel<T> extends Channel<T> {

    private final Stack<T> list;
    private final int limit;
    private final AtomicInteger size;
    private final Object limit_lock = new Object();

    private LimitedChannel(final int limit) {
        this.limit = limit;
        size = new AtomicInteger(0);
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
            
            T res = list.pop();
            size.decrementAndGet();
            synchronized (limit_lock) {
                limit_lock.notify();
            }
            return res;
        }
    }

    @Override
    public void send(final T obj) {
        if (size.get() == limit) {
            synchronized (limit_lock) {
                try {
                    limit_lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeInterruptedException(e);
                }
            }
        }
        synchronized (list) {
            list.add(obj);
            size.incrementAndGet();
            list.notify();
        }
    }

}
