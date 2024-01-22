package net.ripe.db.whois.common;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class InstanceLockTest {

    @Test
    public void testLockMultipleInstances() {
        final String baseDir = "target";

        InstanceLock lock1 = new InstanceLock(baseDir, "whois-aws-");
        InstanceLock lock2 = new InstanceLock(baseDir, "whois-aws-");

        assertThat(lock1.getInstanceName(), is("whois-aws-1"));
        assertThat(lock2.getInstanceName(), is("whois-aws-2"));

        lock1.release();

        lock1 = new InstanceLock(baseDir, "whois-aws-");

        assertThat(lock1.getInstanceName(), is("whois-aws-1"));

        InstanceLock lock3 = new InstanceLock(baseDir, "whois-aws-");
        assertThat(lock3.getInstanceName(), is("whois-aws-3"));

        lock1.release();
        lock2.release();
        lock3.release();
    }

}