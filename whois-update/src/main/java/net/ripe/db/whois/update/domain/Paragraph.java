package net.ripe.db.whois.update.domain;

public class Paragraph {
    private final String content;
    private final Credentials credentials;

    public Paragraph(final String content) {
        this.content = content;
        this.credentials = new Credentials();
    }

    public Paragraph(final String content, final Credentials credentials) {
        this.content = content;
        this.credentials = credentials;
    }

    public String getContent() {
        return content;
    }

    public Credentials getCredentials() {
        return credentials;
    }
}
