package net.ripe.db.whois.api.rdap.domain;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeAdapter extends XmlAdapter<String, LocalDateTime> {
    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Override
    public LocalDateTime unmarshal(final String v) {
        return LocalDateTime.from(ISO_DATE_TIME_FORMATTER.parse(v));
    }

    @Override
    public String marshal(final LocalDateTime v) {
        return ISO_DATE_TIME_FORMATTER.format(v);
    }
}
