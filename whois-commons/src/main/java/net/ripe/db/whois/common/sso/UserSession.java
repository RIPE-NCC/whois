package net.ripe.db.whois.common.sso;

import com.google.common.base.MoreObjects;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class UserSession {

    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
            .appendFraction(ChronoField.MICRO_OF_SECOND, 0, 3, true)
            .appendOffset("+HH:MM", "Z")
            .parseLenient()
            .toFormatter();

    final private String username;
    final private String displayName;
    final private boolean isActive;
    final private LocalDateTime expiryDate;
    final private String uuid;

    public UserSession(final String uuid, final String username, final String displayName, final boolean isActive, final String expiryDate) {
        this.uuid = uuid;
        this.username = username;
        this.displayName = displayName;
        this.isActive = isActive;
        this.expiryDate = expiryDate == null ? LocalDateTime.now().plusHours(1) : LocalDateTime.parse(expiryDate, ISO_DATE_TIME_FORMATTER);
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUsername() {
        return username;
    }

    public boolean isActive() {
        return isActive;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("username", username)
                .add("displayName", displayName)
                .add("isActive", isActive)
                .add("expiryDate", expiryDate)
                .add("uuid", uuid)
                .toString();
    }
}
