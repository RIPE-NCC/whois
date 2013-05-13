package net.ripe.db.whois.update.domain;

import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

public class PasswordCredential implements Credential {
    private final String password;

    public static Set<PasswordCredential> forPasswords(final String... passwords) {
        return forPasswords(Arrays.asList(passwords));
    }

    public static Set<PasswordCredential> forPasswords(final Collection<String> passwords) {
        final Set<PasswordCredential> result = Sets.newLinkedHashSetWithExpectedSize(passwords.size());
        for (final String password : passwords) {
            result.add(new PasswordCredential(password));
        }

        return result;
    }

    public PasswordCredential(final String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PasswordCredential that = (PasswordCredential) o;
        return password.equals(that.password);
    }

    @Override
    public int hashCode() {
        return password.hashCode();
    }

    @Override
    public String toString() {
        return "PasswordCredential{password = '" + password + "'}";
    }
}
