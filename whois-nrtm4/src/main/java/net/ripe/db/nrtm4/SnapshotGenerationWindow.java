package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.whois.common.DateTimeProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class SnapshotGenerationWindow {

    private final LocalTime from;
    private final LocalTime to;
    private final DateTimeProvider dateTimeProvider;

    SnapshotGenerationWindow(
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

    public boolean hasVersionExpired(final NrtmVersionInfo versionToRemove) {
        final long fromMs = from.toEpochSecond(LocalDate.now(), ZoneOffset.UTC);
        final long toMs = to.toEpochSecond(LocalDate.now(), ZoneOffset.UTC);
        final long expireTime = versionToRemove.created() + Math.abs(toMs - fromMs);
        return expireTime < LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
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
