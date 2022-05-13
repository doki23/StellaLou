package com.github.doki23.lock;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class ReentrantSpinLockTest {
    private static class Incr {
        private ReentrantSpinLock lock = new ReentrantSpinLock();
        private int i;

        private void incr() {
            lock.lock();
            i++;
            lock.unlock();
        }

        private int getI() {
            return i;
        }
    }

    @Test
    public void lock() throws InterruptedException {
        Incr incr = new Incr();
        int n = 10;
        int inner_loop = 100_000;
        CountDownLatch boot = new CountDownLatch(n);
        CountDownLatch complete = new CountDownLatch(n);
        for (int i = 0; i < n; ++i) {
            new Thread(() -> {
                try {
                    boot.await();
                    for (int k = 0; k < inner_loop; ++k) {
                        incr.incr();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    complete.countDown();
                }
            }).start();
            boot.countDown();
        }
        complete.await();
        int expect = n * inner_loop;
        int actual = incr.getI();
        assert expect == actual : String.format("expect %d actual %d", expect, actual);
    }

}
