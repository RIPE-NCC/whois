package net.ripe.db.whois.update.domain;

import javax.annotation.concurrent.Immutable;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

@Immutable
public class ContentWithCredentials {
    final String content;
    final List<Credential> credentials;
    final Charset charset;

    public ContentWithCredentials(final String content) {
        this.content = content;
        this.charset = null;
        this.credentials = Collections.emptyList();
    }

    public ContentWithCredentials(final String content, final List<Credential> credentials) {
        this.content = content;
        this.credentials = Collections.unmodifiableList(credentials);
        this.charset = null;
    }

    public ContentWithCredentials(final String content, final Charset charset) {
        this.content = content;
        this.charset = charset;
        this.credentials = Collections.EMPTY_LIST;
    }

    public ContentWithCredentials(final String content, final List<Credential> credentials, final Charset charset) {
        this.content = content;
        this.credentials = Collections.unmodifiableList(credentials);
        this.charset = charset;
    }

    public String getContent() {
        return content;
    }

    public List<Credential> getCredentials() {
        return credentials;
    }

    public Charset getCharset() {
        return charset;
    }
}
