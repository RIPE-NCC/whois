package net.ripe.db.whois.common.support;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static net.ripe.db.whois.common.support.DateMatcher.isBefore;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;


class DateMatcherTest {


    @Test
    public void nowIsNotBeforeNow() {
        final LocalDateTime now = LocalDateTime.now();

        assertThat(now, not(isBefore(now)));
    }

    @Test
    public void yesterdayIsBeforeNow() {
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime yesterday = LocalDateTime.now().minusDays(1L);

        assertThat(now, not(isBefore(yesterday)));
        assertThat(yesterday, isBefore(now));
    }

    @Test
    public void nowIsBeforeTomorrow() {
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime tomorrow = LocalDateTime.now().plusDays(1L);

        assertThat(tomorrow, not(isBefore(now)));
        assertThat(now, isBefore(tomorrow));
    }

}
