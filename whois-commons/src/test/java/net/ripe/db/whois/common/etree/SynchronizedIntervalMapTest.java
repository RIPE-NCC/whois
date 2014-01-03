package net.ripe.db.whois.common.etree;

import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.iptree.Ipv4Entry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SynchronizedIntervalMapTest {
    @Mock private IntervalMap<Ipv4Resource, Ipv4Entry> wrapped;

    private IntervalMap<Ipv4Resource, Ipv4Entry> subject;

    private Ipv4Resource key = Ipv4Resource.parse("127.0.0.1");
    private Ipv4Entry value = new Ipv4Entry(key, 1);

    @Before
    public void setUp() throws Exception {
        subject = SynchronizedIntervalMap.synchronizedMap(wrapped);
    }

    @Test
    public void with_mutex() {
        final Object mutex = new Object();
        subject = SynchronizedIntervalMap.synchronizedMap(wrapped, mutex);

        synchronized (mutex) {
            subject.put(key, value);
            verify(wrapped, times(1)).put(key, value);
        }
    }

    @Test
    public void put() {
        subject.put(key, value);
        verify(wrapped, times(1)).put(key, value);
    }

    @Test
    public void remove() {
        subject.remove(key);
        verify(wrapped, times(1)).remove(key);
    }

    @Test
    public void remove_with_value() {
        subject.remove(key, value);
        verify(wrapped, times(1)).remove(key, value);
    }

    @Test
    public void findFirstLessSpecific() {
        subject.findFirstLessSpecific(key);
        verify(wrapped, times(1)).findFirstLessSpecific(key);
    }

    @Test
    public void findAllLessSpecific() {
        subject.findAllLessSpecific(key);
        verify(wrapped, times(1)).findAllLessSpecific(key);
    }

    @Test
    public void findExactAndAllLessSpecific() {
        subject.findExactAndAllLessSpecific(key);
        verify(wrapped, times(1)).findExactAndAllLessSpecific(key);
    }

    @Test
    public void findExact() {
        subject.findExact(key);
        verify(wrapped, times(1)).findExact(key);
    }

    @Test
    public void findExactOrFirstLessSpecific() {
        subject.findExactOrFirstLessSpecific(key);
        verify(wrapped, times(1)).findExactOrFirstLessSpecific(key);
    }

    @Test
    public void findFirstMoreSpecific() {
        subject.findFirstMoreSpecific(key);
        verify(wrapped, times(1)).findFirstMoreSpecific(key);
    }

    @Test
    public void findAllMoreSpecific() {
        subject.findAllMoreSpecific(key);
        verify(wrapped, times(1)).findAllMoreSpecific(key);
    }

    @Test
    public void findExactAndAllMoreSpecific() {
        subject.findExactAndAllMoreSpecific(key);
        verify(wrapped, times(1)).findExactAndAllMoreSpecific(key);
    }

    @Test
    public void clear() {
        subject.clear();
        verify(wrapped, times(1)).clear();
    }
}
