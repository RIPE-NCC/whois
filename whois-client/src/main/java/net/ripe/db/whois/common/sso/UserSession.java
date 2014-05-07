package net.ripe.db.whois.common.sso;

import org.joda.time.LocalDateTime;
import org.joda.time.format.ISODateTimeFormat;

public class UserSession {
    final private String username;
    final private boolean isActive;
    final private LocalDateTime expiryDate;
    private String uuid;

    public UserSession(String username, boolean isActive, String expiryDate) {
        this.username = username;
        this.isActive = isActive;
        this.expiryDate = expiryDate == null ? LocalDateTime.now().plusHours(1) : LocalDateTime.parse(expiryDate, ISODateTimeFormat.dateTimeParser());
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

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return "UserSession{" +
                "username='" + username + '\'' +
                ", isActive=" + isActive +
                ", uuid='" + uuid + '\'' +
                '}';
    }
}
