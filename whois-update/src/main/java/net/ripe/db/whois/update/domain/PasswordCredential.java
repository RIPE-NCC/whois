package net.ripe.db.whois.update.domain;

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
