package net.ripe.db.whois.api.whois;

import net.ripe.db.whois.common.DateTimeProvider;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SyncUpdateTest {
    @Mock DateTimeProvider dateTimeProvider;
    SyncUpdate subject;

    private static final String LOCALHOST = "127.0.0.1";

    @Before
    public void setUp() throws Exception {
        when(dateTimeProvider.getCurrentDateTime()).thenReturn(new LocalDateTime(0, DateTimeZone.UTC));
        subject = new SyncUpdate(dateTimeProvider, LOCALHOST);
    }

    @Test
    public void getId() throws Exception {
        assertThat(subject.getId(), is(LOCALHOST));
    }

    @Test
    public void getFrom() throws Exception {
        assertThat(subject.getFrom(), is(LOCALHOST));
    }

    @Test
    public void getName() throws Exception {
        assertThat(subject.getName(), is("sync update"));
    }

    @Test
    public void response_header_for_any_request() throws Exception {
        assertThat(subject.getResponseHeader(), containsString("" +
                " - From-Host: 127.0.0.1\n" +
                " - Date/Time: Thu Jan 1 00:00:00 1970\n"));
    }

    @Test
    public void notification_header_for_any_request() throws Exception {
        assertThat(subject.getNotificationHeader(), containsString("" +
                " - From-Host: 127.0.0.1\n" +
                " - Date/Time: Thu Jan 1 00:00:00 1970\n"));
    }
}
