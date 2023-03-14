package net.ripe.db.nrtm4;

import net.ripe.db.whois.common.DateTimeProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class SnapshotWindow {

    private final LocalTime from;
    private final LocalTime to;
    private final DateTimeProvider dateTimeProvider;

    SnapshotWindow(
        @Value("${nrtm.snapshot.window}") final String windowDefinition,
        final DateTimeProvider dateTimeProvider
    ) {
        this.dateTimeProvider = dateTimeProvider;
        final Pattern windowDefinitionPattern = Pattern.compile("(\\d{2}):(\\d{2})\\s*-\\s*(\\d{2}):(\\d{2})");
        final Matcher matcher = windowDefinitionPattern.matcher(windowDefinition);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Bad nrtm.snapshot.window string. Must be sth like 00:30 - 04:00");
        }
        from = LocalTime.of(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
        to = LocalTime.of(Integer.parseInt(matcher.group(3)), Integer.parseInt(matcher.group(4)));
    }

    public boolean isInWindow() {
        final LocalTime localTime = dateTimeProvider.getCurrentDateTime().toLocalTime();
        if (from.isBefore(to)) {
            return localTime.isAfter(from) && localTime.isBefore(to);
        } else {
            return localTime.isAfter(from) || localTime.isBefore(to);
        }
    }

}
