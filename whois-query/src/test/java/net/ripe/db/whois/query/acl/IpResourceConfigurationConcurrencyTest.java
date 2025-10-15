package net.ripe.db.whois.query.acl;

import com.google.common.base.Stopwatch;
import net.ripe.db.whois.common.domain.IpResourceEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IpResourceConfigurationConcurrencyTest {
    private volatile boolean stop;

    @Mock private IpResourceConfiguration.Loader loader;

    private IpResourceConfiguration subject;

    @BeforeEach
    public void setup() {
        when(loader.loadIpLimits()).thenReturn(Collections.<IpResourceEntry<Integer>>emptyList());
        when(loader.loadIpProxy()).thenReturn(Collections.<IpResourceEntry<Boolean>>emptyList());
        when(loader.loadIpDenied()).thenReturn(Collections.<IpResourceEntry<Boolean>>emptyList());

        subject = new IpResourceConfiguration(loader, 5000);
        subject.reload();
    }

    @Test
    public void test_concurrent_access() throws Exception {
        final ExecutorService executor = Executors.newCachedThreadPool();

        final List<Future<Exception>> result = new ArrayList<>();
        try {
            result.add(executor.submit(makeReader()));
            result.add(executor.submit(makeReader()));
            result.add(executor.submit(makeReader()));
            result.add(executor.submit(makeReader()));  

            final Stopwatch stopwatch = Stopwatch.createStarted();
            while (stopwatch.elapsed(TimeUnit.MILLISECONDS) < 100) {
                subject.reload();
                Thread.sleep(10);
            }
        } finally {
            stop = true;
            executor.shutdown();
        }

        for (Future<Exception> f : result) {
            assertThat(f.get(), is(nullValue()));
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
