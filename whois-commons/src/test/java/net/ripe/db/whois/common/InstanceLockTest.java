package net.ripe.db.whois.common;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class InstanceLockTest {

    @Test
    public void testLockMultipleInstances() {
        final String baseDir = "target";

        InstanceLock lock1 = new InstanceLock(baseDir);
        InstanceLock lock2 = new InstanceLock(baseDir);

        assertThat(lock1.getInstanceName(), is(InstanceLock.INSTANCE_NAME_PREFIX + "1"));
        assertThat(lock2.getInstanceName(), is(InstanceLock.INSTANCE_NAME_PREFIX + "2"));

        lock1.release();

        lock1 = new InstanceLock(baseDir);

        assertThat(lock1.getInstanceName(), is(InstanceLock.INSTANCE_NAME_PREFIX + "1"));

        InstanceLock lock3 = new InstanceLock(baseDir);
        assertThat(lock3.getInstanceName(), is(InstanceLock.INSTANCE_NAME_PREFIX + "3"));

        lock1.release();
        lock2.release();
        lock3.release();
    }

}