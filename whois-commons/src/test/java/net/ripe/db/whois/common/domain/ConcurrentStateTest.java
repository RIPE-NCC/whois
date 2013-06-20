package net.ripe.db.whois.common.domain;

import com.jayway.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentStateTest {

    private ConcurrentState subject;

    @Before
    public void setup() {
        subject = new ConcurrentState();
    }

    @Test
    public void set_and_unset_state() {
        Counter counter = new Counter();
        new Thread(counter).start();

        subject.set(true);
        waitForExpectedValue(counter, 1);

        subject.set(false);
        subject.set(false);
        subject.set(false);
        waitForExpectedValue(counter, 2);

        subject.set(true);
        waitForExpectedValue(counter, 3);
    }

    @Test
    public void set_and_unset_state_multiple_updates() {
        subject.set(true);
        subject.set(false);
        subject.set(false);
        subject.set(false);
        subject.set(true);

        Counter counter = new Counter();
        new Thread(counter).start();

        waitForExpectedValue(counter, 1);
    }

    private void waitForExpectedValue(final Counter counter, final int expectedValue) {
        Awaitility.await().until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return counter.getCount() == expectedValue;
            }
        });
    }

    private class Counter implements Runnable {
        private AtomicInteger count = new AtomicInteger();

        @Override
        public void run() {
            boolean nextState = true;
            for (;;) {
                subject.waitUntil(nextState);
                count.incrementAndGet();
                nextState = !nextState;
            }
        }

        private int getCount() {
            return count.get();
        }
    }
}
