package net.ripe.db.whois.common.sso.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class EventDateTimeAdapterTest {

    private final EventDateTimeAdapter subject = new EventDateTimeAdapter();

    @Test
    public void unmarshal() {
        assertThat(subject.unmarshal("2007-12-03T10:15:30.000Z"), is(LocalDateTime.parse("2007-12-03T10:15:30")));
        assertThat(subject.unmarshal("2012-05-08T12:32:00.000Z"), is(LocalDateTime.parse("2012-05-08T12:32:00")));
        assertThat(subject.unmarshal("2024-06-24T06:16:36.1Z"), is(LocalDateTime.parse("2024-06-24T06:16:36.000000001")));
        assertThat(subject.unmarshal("2024-06-24T06:16:36.12Z"), is(LocalDateTime.parse("2024-06-24T06:16:36.000000012")));
        assertThat(subject.unmarshal("2024-06-24T06:16:36.123Z"), is(LocalDateTime.parse("2024-06-24T06:16:36.000000123")));
        assertThat(subject.unmarshal("2024-06-24T06:16:36.275379Z"), is(LocalDateTime.parse("2024-06-24T06:16:36.000275379")));
        assertThat(subject.unmarshal("2024-06-24T06:16:36.275379000Z"), is(LocalDateTime.parse("2024-06-24T06:16:36.275379000")));
    }

    @Test
    public void marshal() {
        assertThat(fromUtc(subject.marshal(LocalDateTime.parse("2007-12-03T10:15:30"))), is("2007-12-03T10:15:30"));
        assertThat(fromUtc(subject.marshal(LocalDateTime.parse("2024-06-24T06:16:36"))), is("2024-06-24T06:16:36"));
    }

    public String fromUtc(final String utcDateTime) {
        return ZonedDateTime.parse(utcDateTime).withZoneSameInstant(ZoneOffset.systemDefault()).toLocalDateTime().toString();
    }
}
