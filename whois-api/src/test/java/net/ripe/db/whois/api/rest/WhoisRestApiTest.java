package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.common.DateTimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WhoisRestApiTest {
    @Mock DateTimeProvider dateTimeProvider;

    private WhoisRestApi subject;

    @BeforeEach
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
        when(dateTimeProvider.getCurrentDateTime()).thenReturn(LocalDateTime.of(2013, 3, 3, 12, 55));

        assertThat(subject.getResponseHeader(), is(" - From-Host: 127.0.0.1\n - Date/Time: Sun Mar 3 12:55:00 2013Z\n"));
    }

    @Test
    public void notification_header_for_any_request() {
        when(dateTimeProvider.getCurrentDateTime()).thenReturn(LocalDateTime.of(2013, 3, 3, 12, 55));

        assertThat(subject.getNotificationHeader(), is(" - From-Host: 127.0.0.1\n - Date/Time: Sun Mar 3 12:55:00 2013Z\n"));
    }
}
