package net.ripe.db.whois.common.apiKey;

import com.google.common.base.MoreObjects;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OAuthSession {

    private final String application;

    private final String email;

    private final String uuid;

    private final LocalDateTime expirationDate;

    private final List<String> scopes;

    public OAuthSession(String application, String email, String uuid, LocalDateTime expirationDate, List<String> scopes) {
        this.application = application;
        this.email = email;
        this.uuid = uuid;
        this.expirationDate = expirationDate;
        this.scopes = scopes;
    }

    public String getApplication() {
        return application;
    }

    public String getEmail() {
        return email;
    }

    public String getUuid() {
        return uuid;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public List<String> getScopes() {
        return scopes;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("application", application)
                .add("email", email)
                .add("uuid", uuid)
                .add("expirationDate", expirationDate.toString())
                .add("scopes", scopes)
                .toString();
    }
}
