package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.common.DateTimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SyncUpdateTest {
    @Mock DateTimeProvider dateTimeProvider;
    SyncUpdate subject;

    private static final String LOCALHOST = "127.0.0.1";

    @BeforeEach
    public void setUp() throws Exception {
        subject = new SyncUpdate(dateTimeProvider, LOCALHOST);
    }

    @Test
    public void getId() {
        assertThat(subject.getId(), is(LOCALHOST));
    }

    @Test
    public void getFrom() {
        assertThat(subject.getFrom(), is(LOCALHOST));
    }

    @Test
    public void getName() {
        assertThat(subject.getName(), is("sync update"));
    }

    @Test
    public void response_header_for_any_request() {
        when(dateTimeProvider.getCurrentDateTime()).thenReturn(LocalDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC));

        assertThat(subject.getResponseHeader(), containsString("" +
                " - From-Host: 127.0.0.1\n" +
                " - Date/Time: Thu Jan 1 00:00:00 1970Z\n"));
    }

    @Test
    public void notification_header_for_any_request() {
        when(dateTimeProvider.getCurrentDateTime()).thenReturn(LocalDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC));

        assertThat(subject.getNotificationHeader(), containsString("" +
                " - From-Host: 127.0.0.1\n" +
                " - Date/Time: Thu Jan 1 00:00:00 1970Z\n"));
    }
}
