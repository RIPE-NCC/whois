package net.ripe.db.whois.update.domain;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertNotNull;

public class UpdateStatusTest {
    @Test
    public void getStatus() {
        for (final UpdateStatus updateStatus : UpdateStatus.values()) {
            assertNotNull(updateStatus.getStatus());
        }
    }
}
