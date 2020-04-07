package net.ripe.db.whois.api.rdap.domain;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

    @Override
    public LocalDateTime unmarshal(final String v) {
        return  LocalDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(v));
    }

    @Override
    public String marshal(final LocalDateTime v) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(v.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC")));
    }

}
