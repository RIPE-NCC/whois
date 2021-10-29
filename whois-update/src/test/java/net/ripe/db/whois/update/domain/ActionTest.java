package net.ripe.db.whois.update.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ActionTest {
    @Test
    public void not_null() {
        for (final Action action : Action.values()) {
            assertNotNull(action.getDescription());
        }
    }
}
