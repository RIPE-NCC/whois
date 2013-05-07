package net.ripe.db.whois.update.domain;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class UpdateStatusTest {
    @Test
    public void getStatus() {
        for (final UpdateStatus updateStatus : UpdateStatus.values()) {
            assertNotNull(updateStatus.getStatus());
        }
    }
}
