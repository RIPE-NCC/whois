package net.ripe.db.whois.common.sso.domain;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class EventDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.n'Z'");

    @Override
    public LocalDateTime unmarshal(final String v) {
        return LocalDateTime.from(DATE_TIME_FORMATTER.parse(v));
    }

    @Override
    public String marshal(final LocalDateTime v) {
        return DATE_TIME_FORMATTER.format(v.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC")));
    }

}
