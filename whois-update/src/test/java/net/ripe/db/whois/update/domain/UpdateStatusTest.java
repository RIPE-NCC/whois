package net.ripe.db.whois.update.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UpdateStatusTest {
    @Test
    public void getStatus() {
        for (final UpdateStatus updateStatus : UpdateStatus.values()) {
            assertNotNull(updateStatus.getStatus());
        }
    }
}
