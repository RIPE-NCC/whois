package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.AbstractIntegrationTest;

import java.time.Duration;
import java.time.Instant;

/**
 * This class is a 'hack' to facilitate local testing.
 */
public abstract class YieldToTestServer {

    public static void yield(final AbstractIntegrationTest integrationTest) {

        Instant start = Instant.now();

        while (!Thread.currentThread().isInterrupted()) {
            try {
                    Instant end = Instant.now();
                    Duration timeElapsed = Duration.between(start, end);
                    System.out.println(String.format("Server listening for %d minutes on port %d", timeElapsed.toMinutes(), integrationTest.getPort()));
                    integrationTest.wait(60000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}