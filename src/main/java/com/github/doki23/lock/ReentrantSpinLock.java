package com.github.doki23.lock;

import java.util.concurrent.atomic.AtomicReference;

/**
 * reentrant spin lock
 * usage:
 * public class TClass {
 * private ReentrantLock lock = new ReentrantSpinLock();
 * <p>
 * public void f() {
 * lock.lock();
 * // do something
 * lock.unlock();
 * }
 * }
 */
public class ReentrantSpinLock {
    private static final ThreadLocal<InnerState> _INNER_STATE = ThreadLocal.withInitial(InnerState::new);

    private static class InnerState {
        private final Thread owner;
        private int cnt;

        public InnerState() {
            this.owner = Thread.currentThread();
            this.cnt = 1;
        }
    }

    private final AtomicReference<InnerState> state = new AtomicReference<>();

    public void lock() {
        Thread currentThread = Thread.currentThread();
        if (!ownedBy(currentThread)) {
            InnerState inner = _INNER_STATE.get();
            while (!state.compareAndSet(null, inner)) {
                // do nothing
            }
        } else {
            // reentrant
            ++_INNER_STATE.get().cnt;
        }
    }

    public void unlock() {
        if (!ownedByCurrentThread()) {
            throw new IllegalStateException("get lock first");
        }
        InnerState innerState = _INNER_STATE.get();
        --innerState.cnt;
        if (innerState.cnt == 0) {
            if (!state.compareAndSet(innerState, null)) {
                throw new IllegalStateException("Lost the lock! This is a bug of ReentrantSpinLock!");
            } else {
                _INNER_STATE.remove();
            }
        }
    }

    public boolean ownedBy(Thread t) {
        InnerState innerState = state.get();
        return innerState != null && innerState.owner == t;
    }

    public boolean ownedByCurrentThread() {
        return ownedBy(Thread.currentThread());
    }
}
