package net.ripe.db.whois.update.domain;

public class Paragraph {
    private final String content;
    private final Credentials credentials;
    private final boolean dryRun;

    public Paragraph(final String content) {
        this(content, new Credentials());
    }

    public Paragraph(final String content, final Credentials credentials) {
        this(content, credentials, false);
    }

    public Paragraph(final String content, final Credentials credentials, final boolean dryRun) {
        this.content = content;
        this.credentials = credentials;
        this.dryRun = dryRun;
    }

    public String getContent() {
        return content;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public boolean isDryRun() {
        return dryRun;
    }
}
