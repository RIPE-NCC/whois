package net.ripe.db.whois.db;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.support.AbstractDaoIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Test to ensure that the Retry aspect is properly applied from whois-commons onto whois sub-modules.
 * <p/>
 * This test also fails running in your IDE if AspectJ is not configured correctly.
 */

@Category(IntegrationTest.class)
@ContextConfiguration(locations = {"classpath:applicationContext-whois-test.xml"})
public class RetryForAspectIntegrationTest extends AbstractDaoIntegrationTest {
    static final int ATTEMPTS = 5;

    AtomicInteger attemptCounter;

    @Autowired RetryForAspectOnClass retryForAspectOnClass;

    @Before
    public void setUp() throws Exception {
        attemptCounter = new AtomicInteger();
    }

    @Test
    public void retryForAnnotatedMethod_exception() throws Exception {
        retryForAnnotatedMethod(new IOException(), ATTEMPTS);
    }

    @Test
    public void retryForAnnotatedMethod_exception_subclass() throws Exception {
        retryForAnnotatedMethod(new FileNotFoundException(), ATTEMPTS);
    }

    @Test
    public void retryForAnnotatedMethod_exception_not_retried() throws Exception {
        retryForAnnotatedMethod(new IllegalStateException(), 1);
    }

    private void retryForAnnotatedMethod(final Exception e, final int expectedAttempts) throws Exception {
        try {
            retryForAspectOnClass.incrementAndThrowException(attemptCounter, e);
            fail("Expected exception");
        } catch (Exception exc) {
            assertThat(e, is(exc));
        }

        assertThat(attemptCounter.get(), is(expectedAttempts));
    }

    @Test
    public void retryForAnnotatedType_exception() throws Exception {
        retryForAnnotatedType(new IOException(), ATTEMPTS);
    }

    @Test
    public void retryForAnnotatedType_exception_subclass() throws Exception {
        retryForAnnotatedType(new FileNotFoundException(), ATTEMPTS);
    }

    @Test
    public void retryForAnnotatedType_exception_not_retried() throws Exception {
        retryForAnnotatedType(new IllegalStateException(), 1);
    }

    private void retryForAnnotatedType(final Exception e, final int expectedAttempts) throws Exception {
        try {
            incrementAndThrowException(attemptCounter, e);
            fail("Expected exception");
        } catch (Exception exc) {
            assertThat(e, is(exc));
        }

        assertThat(attemptCounter.get(), is(expectedAttempts));
    }

    @RetryFor(value = IOException.class, attempts = RetryForAspectIntegrationTest.ATTEMPTS, intervalMs = 0)
    public void incrementAndThrowException(final AtomicInteger counter, final Exception e) throws Exception {
        counter.incrementAndGet();
        throw e;
    }
}
