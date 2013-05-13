package net.ripe.db.whois.update.domain;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ActionTest {
    @Test
    public void not_null() {
        for (final Action action : Action.values()) {
            assertNotNull(action.getDescription());
        }
    }
}
