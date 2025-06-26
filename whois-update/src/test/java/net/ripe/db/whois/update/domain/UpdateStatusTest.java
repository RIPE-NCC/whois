package net.ripe.db.whois.update.domain;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class UpdateStatusTest {
    @Test
    public void getStatus() {
        for (final UpdateStatus updateStatus : UpdateStatus.values()) {
            assertThat(updateStatus.getStatus(), not(nullValue()));
        }
    }
}
