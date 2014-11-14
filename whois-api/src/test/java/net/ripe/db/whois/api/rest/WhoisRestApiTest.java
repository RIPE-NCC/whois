package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.common.DateTimeProvider;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WhoisRestApiTest {
    @Mock DateTimeProvider dateTimeProvider;

    private WhoisRestApi subject;

    @Before
    public void setup() {
        subject = new WhoisRestApi(dateTimeProvider, "127.0.0.1");
    }

    @Test
    public void getId() {
        assertThat(subject.getId(), is("127.0.0.1"));
    }

    @Test
    public void getFrom() {
        assertThat(subject.getFrom(), is("127.0.0.1"));
    }

    @Test
    public void getName() {
        assertThat(subject.getName(), is("rest api"));
    }

    @Test
    public void response_header_for_any_request() {
        when(dateTimeProvider.getCurrentDateTime()).thenReturn(new LocalDateTime(2013, 3, 3, 12, 55));

        assertThat(subject.getResponseHeader(), is(" - From-Host: 127.0.0.1\n - Date/Time: Sun Mar 3 12:55:00 2013\n"));
    }

    @Test
    public void notification_header_for_any_request() {
        when(dateTimeProvider.getCurrentDateTime()).thenReturn(new LocalDateTime(2013, 3, 3, 12, 55));

        assertThat(subject.getNotificationHeader(), is(" - From-Host: 127.0.0.1\n - Date/Time: Sun Mar 3 12:55:00 2013\n"));
    }
}
