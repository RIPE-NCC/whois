package net.ripe.db.whois.update.domain;

import com.google.common.base.Charsets;

import javax.annotation.concurrent.Immutable;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

@Immutable
public class ContentWithCredentials {
    final String content;
    final List<Credential> credentials;
    final Charset charset;

    public static ContentWithCredentials contentWithCredentialsInIso88591(final String content) {
        return new ContentWithCredentials(content, Charsets.ISO_8859_1);
    }

    public static ContentWithCredentials contentWithCredentialsInIso88591(final String content, final List<Credential> credentials) {
        return new ContentWithCredentials(content, Collections.unmodifiableList(credentials), Charsets.ISO_8859_1);
    }

    public ContentWithCredentials(final String content, final Charset charset) {
        this.content = content;
        this.charset = charset;
        this.credentials = Collections.emptyList();
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
