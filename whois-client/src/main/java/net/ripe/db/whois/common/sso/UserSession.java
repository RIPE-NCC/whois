package net.ripe.db.whois.common.sso;

import com.google.common.base.MoreObjects;
import org.joda.time.LocalDateTime;
import org.joda.time.format.ISODateTimeFormat;

public class UserSession {
    final private String username;
    final private String displayName;
    final private boolean isActive;
    final private LocalDateTime expiryDate;
    private String uuid;

    public UserSession(final String username, final String displayName, final boolean isActive, final String expiryDate) {
        this.username = username;
        this.displayName = displayName;
        this.isActive = isActive;
        this.expiryDate = expiryDate == null ? LocalDateTime.now().plusHours(1) : LocalDateTime.parse(expiryDate, ISODateTimeFormat.dateTimeParser());
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

    public void setUuid(final String uuid) {
        this.uuid = uuid;
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
