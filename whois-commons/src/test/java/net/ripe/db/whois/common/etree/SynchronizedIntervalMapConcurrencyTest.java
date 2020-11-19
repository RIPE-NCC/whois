package net.ripe.db.whois.common.etree;

import com.google.common.base.Stopwatch;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.iptree.Ipv4Entry;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;

public class SynchronizedIntervalMapConcurrencyTest {
    private final IntervalMap<Ipv4Resource, Ipv4Entry> subject = SynchronizedIntervalMap.synchronizedMap(new NestedIntervalMap<Ipv4Resource, Ipv4Entry>());

    private volatile boolean stop;

    @Test
    public void should_deal_with_concurrent_access() throws Exception {
        final ExecutorService executor = Executors.newCachedThreadPool();

        final List<Future<Exception>> result = new ArrayList<>();
        try {
            final Ipv4Entry entry = new Ipv4Entry(new Ipv4Resource(5, 6), 2);
            subject.put(entry.getKey(), entry);

            result.add(executor.submit(makeWriter(new Ipv4Resource(1, 10))));
            result.add(executor.submit(makeWriter(new Ipv4Resource(4, 4))));
            result.add(executor.submit(makeWriter(new Ipv4Resource(5, 5))));
            result.add(executor.submit(makeWriter(new Ipv4Resource(7, 7))));

            final Stopwatch stopwatch = Stopwatch.createStarted();
            while (stopwatch.elapsed(TimeUnit.MILLISECONDS) < 100) {
                final List<Ipv4Entry> match = subject.findExact(entry.getKey());
                assertThat(match, contains(entry));
            }
        } finally {
            stop = true;
            executor.shutdown();
        }
        for (Future<Exception> f : result) {
            assertEquals(null, f.get());
        }
    }

    private Callable<Exception> makeWriter(final Ipv4Resource resource) {
        return new Callable<Exception>() {
            @Override
            public Exception call() {
                try {
                    final Ipv4Entry entry = new Ipv4Entry(resource, 1);
                    while (!stop) {
                        subject.put(entry.getKey(), entry);
                        subject.remove(entry.getKey());
                    }
                    return null;
                } catch (Exception e) {
                    return e;
                }
            }
        };
    }
}
