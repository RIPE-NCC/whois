package net.ripe.db.whois.update.domain;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;


public class ActionTest {
    @Test
    public void not_null() {
        for (final Action action : Action.values()) {
            assertThat(action.getDescription(), not(nullValue()));
        }
    }
}
