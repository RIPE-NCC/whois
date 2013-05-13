package net.ripe.db.whois.update.domain;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.List;

@Immutable
public class ContentWithCredentials {
    final String content;
    final List<Credential> credentials;

    public ContentWithCredentials(final String content) {
        this.content = content;
        this.credentials = Collections.emptyList();
    }

    public ContentWithCredentials(final String content, final List<Credential> credentials) {
        this.content = content;
        this.credentials = Collections.unmodifiableList(credentials);
    }

    public String getContent() {
        return content;
    }

    public List<Credential> getCredentials() {
        return credentials;
    }
}
