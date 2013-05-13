package net.ripe.db.whois.common.pipeline;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SafeStopwatchTest {
    SafeStopwatch subject;

    @Before
    public void setUp() throws Exception {
        subject = new SafeStopwatch();
    }

    @Test
    public void start_stop() {
        assertFalse(subject.start());
        assertNotNull(subject.stop());
    }

    @Test
    public void start_thrice() {
        assertFalse(subject.start());
        assertTrue(subject.start());
        assertTrue(subject.start());
    }

    @Test
    public void stop_thrice() {
        assertFalse(subject.start());
        assertNotNull(subject.stop());
        assertNull(subject.stop());
        assertNull(subject.stop());
    }

    @Test
    public void stop_without_start() {
        assertNull(subject.stop());
    }
}
