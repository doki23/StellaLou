package com.github.doki23.lock;

import java.util.concurrent.atomic.AtomicReference;

/**
 * reentrant spin lock
 * usage:
 * public class TClass {
 *     private ReentrantLock lock = new ReentrantSpinLock();
 *
 *     public void f() {
 *         lock.lock();
 *         // do something
 *         lock.unlock();
 *     }
 * }
 */
public class ReentrantSpinLock {
    private final AtomicReference<Thread> owner = new AtomicReference<>();

    public void lock() {
        Thread currentThread = Thread.currentThread();
        if (!ownedBy(currentThread)) {
            while (!owner.compareAndSet(null, currentThread)) {}
        }
    }

    public void unlock() {
        if (!owner.compareAndSet(Thread.currentThread(), null)) {
            throw new IllegalStateException("get lock first");
        }
    }

    public boolean ownedBy(Thread t) {
        return owner.get() == t;
    }

    public boolean ownedByCurrentThread() {
        return ownedBy(Thread.currentThread());
    }
}
