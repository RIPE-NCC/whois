package net.ripe.db.whois.common.aspects;


import net.ripe.db.whois.common.support.AbstractDaoIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("IntegrationTest")
public class RetryForAspectIntegrationTest extends AbstractDaoIntegrationTest {
    static final int ATTEMPTS = 5;

    AtomicInteger attemptCounter;

    @Autowired RetryForAspectMethod retryForAspectMethod;
    @Autowired RetryForAspectType retryForAspectType;

    @BeforeEach
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
            retryForAspectMethod.incrementAndThrowException(attemptCounter, e);
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
            retryForAspectType.incrementAndThrowException(attemptCounter, e);
            fail("Expected exception");
        } catch (Exception exc) {
            assertThat(e, is(exc));
        }

        assertThat(attemptCounter.get(), is(expectedAttempts));
    }
}
