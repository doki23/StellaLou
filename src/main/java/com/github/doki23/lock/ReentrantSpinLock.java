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
    private static final ThreadLocal<InnerState> _INNER_STATE = new ThreadLocal<>();

    private static class InnerState {
        private int cnt;

        public InnerState() {
            this.cnt = 1;
        }
    }

    private final AtomicReference<InnerState> state = new AtomicReference<>();

    public void lock() {
        if (!ownedByCurrentThread()) {
            InnerState inner = new InnerState();
            _INNER_STATE.set(inner);
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

    public boolean ownedByCurrentThread() {
        return _INNER_STATE.get() != null &&_INNER_STATE.get() == state.get();
    }
}
