package net.ripe.db.whois.common.Credentials;

import java.util.Objects;

public class PasswordCredential implements Credential {
    private final String password;

    public PasswordCredential(final String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final PasswordCredential that = (PasswordCredential) o;

        return Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(password);
    }

    @Override
    public String toString() {
        return "PasswordCredential";
    }

}
