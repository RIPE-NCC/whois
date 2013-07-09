package net.ripe.db.whois.api.whois.rdap.domain;

import org.apache.commons.lang.NotImplementedException;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DateTimeAdapter extends XmlAdapter<String, LocalDateTime> {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Override
    public LocalDateTime unmarshal(final String v) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public String marshal(final LocalDateTime v) throws Exception {
        return DATE_TIME_FORMATTER.print(v);
    }
}
