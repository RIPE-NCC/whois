package net.ripe.db.whois.update.domain;

//TODO [AS] This is a placeholder. Expand when requirements arrive.
public class SSOCredential implements Credential {

    private final String token;

    public SSOCredential(final String token) {
        this.token = token;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final SSOCredential that = (SSOCredential) o;

        if (!token.equals(that.token)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return token.hashCode();
    }
}
