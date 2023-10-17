package net.ripe.db.whois.update.domain;

import java.util.Objects;

public record PasswordCredential(String password) implements Credential {

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final PasswordCredential that = (PasswordCredential) o;

        return Objects.equals(password, that.password);
    }

    @Override
    public String toString() {
        return "PasswordCredential";
    }

}
