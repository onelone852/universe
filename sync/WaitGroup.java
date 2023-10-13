package sync;

import java.util.concurrent.atomic.AtomicInteger;

public final class WaitGroup {

    private final AtomicInteger waiter;
    private final Object lock = new Object();

    public WaitGroup() {
        waiter = new AtomicInteger(0);
    }

    public void Wait() {
        if (waiter.get() != 0) {
            try {
                synchronized (lock) {
                    lock.wait();
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    public void Work() {
        waiter.addAndGet(1);
    }

    public void Done() {
        if (waiter.addAndGet(-1) == 0) {
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }
    
}
