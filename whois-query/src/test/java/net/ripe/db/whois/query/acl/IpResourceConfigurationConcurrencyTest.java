package net.ripe.db.whois.query.acl;

import com.google.common.base.Stopwatch;
import net.ripe.db.whois.common.domain.IpResourceEntry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IpResourceConfigurationConcurrencyTest {
    private volatile boolean stop;

    @Mock private IpResourceConfiguration.Loader loader;
    @InjectMocks private IpResourceConfiguration subject;

    @Before
    public void setup() {
        when(loader.loadIpLimit()).thenReturn(Collections.<IpResourceEntry<Integer>>emptyList());
        when(loader.loadIpProxy()).thenReturn(Collections.<IpResourceEntry<Boolean>>emptyList());
        when(loader.loadIpDenied()).thenReturn(Collections.<IpResourceEntry<Boolean>>emptyList());

        subject.reload();
    }

    @Test
    public void test_concurrent_access() throws Exception {
        ExecutorService executor = Executors.newCachedThreadPool();

        List<Future<Exception>> result = new ArrayList<Future<Exception>>();
        try {
            result.add(executor.submit(makeReader()));
            result.add(executor.submit(makeReader()));
            result.add(executor.submit(makeReader()));
            result.add(executor.submit(makeReader()));

            Stopwatch stopwatch = new Stopwatch();
            stopwatch.start();
            while (stopwatch.elapsedMillis() < 100) {
                subject.reload();
                Thread.sleep(10);
            }
        } finally {
            stop = true;
            executor.shutdown();
        }

        for (Future<Exception> f : result) {
            assertEquals(null, f.get());
        }
    }

    private Callable<Exception> makeReader() {
        return new Callable<Exception>() {
            @Override
            public Exception call() {
                try {
                    while (!stop) {
                        subject.isProxy(InetAddress.getByName("128.0.0.1"));
                    }
                    return null;
                } catch (Exception e) {
                    return e;
                }
            }
        };
    }
}
