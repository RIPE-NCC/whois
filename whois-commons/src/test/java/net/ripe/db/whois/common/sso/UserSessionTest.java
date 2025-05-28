package net.ripe.db.whois.common.sso;

import net.ripe.db.whois.common.sso.UserSession;
import org.junit.jupiter.api.Test;

import java.time.Month;
import java.time.temporal.ChronoField;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class UserSessionTest {

    @Test
    public void testTimestampParsingNoMillis() {
        UserSession userSession = new UserSession("1","username", "displayName", true, "2019-09-19T14:51:05+02:00");

        assertThat(userSession.getExpiryDate().getYear(), is(2019));
        assertThat(userSession.getExpiryDate().getMonth(), is(Month.SEPTEMBER));
        assertThat(userSession.getExpiryDate().getDayOfMonth(), is(19));
        assertThat(userSession.getExpiryDate().getHour(), is(14));
        assertThat(userSession.getExpiryDate().getMinute(), is(51));
        assertThat(userSession.getExpiryDate().getSecond(), is(5));
    }

    @Test
    public void testTimestampParsingWithMillis() {
        UserSession userSession = new UserSession("2","username", "displayName", true, "2019-09-19T20:16:43.835+02:00");

        assertThat(userSession.getExpiryDate().getYear(), is(2019));
        assertThat(userSession.getExpiryDate().getMonth(), is(Month.SEPTEMBER));
        assertThat(userSession.getExpiryDate().getDayOfMonth(), is(19));
        assertThat(userSession.getExpiryDate().getHour(), is(20));
        assertThat(userSession.getExpiryDate().getMinute(), is(16));
        assertThat(userSession.getExpiryDate().getSecond(), is(43));
        assertThat(userSession.getExpiryDate().get(ChronoField.MILLI_OF_SECOND), is(835));
    }

    @Test
    public void testTimestampParsingNoOffset() {
        UserSession userSession = new UserSession("3","username", "displayName", true, "2019-09-19T20:16:43.835Z");

        assertThat(userSession.getExpiryDate().getYear(), is(2019));
        assertThat(userSession.getExpiryDate().getMonth(), is(Month.SEPTEMBER));
        assertThat(userSession.getExpiryDate().getDayOfMonth(), is(19));
        assertThat(userSession.getExpiryDate().getHour(), is(20));
        assertThat(userSession.getExpiryDate().getMinute(), is(16));
        assertThat(userSession.getExpiryDate().getSecond(), is(43));
        assertThat(userSession.getExpiryDate().get(ChronoField.MILLI_OF_SECOND), is(835));
    }

}
