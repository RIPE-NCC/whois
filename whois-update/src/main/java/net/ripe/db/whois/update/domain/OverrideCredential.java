package net.ripe.db.whois.update.domain;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Immutable
public class OverrideCredential implements Credential {
    private static final Splitter OVERRIDE_SPLITTER = Splitter.on(',').limit(3);
    private static final String DEFAULT_USER_1 = "dbase1";
    private static final String DEFAULT_USER_2 = "dbase2";

    private final String value;
    private final Set<UsernamePassword> possibleCredentials;
    private final String remarks;

    private OverrideCredential(final String value, final Set<UsernamePassword> possibleCredentials, final String remarks) {
        this.value = value;
        this.possibleCredentials = possibleCredentials;
        this.remarks = remarks;
    }

    public Set<UsernamePassword> getPossibleCredentials() {
        return possibleCredentials;
    }

    public String getRemarks() {
        return remarks;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final OverrideCredential that = (OverrideCredential) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }

    public static OverrideCredential parse(final String value) {
        final List<String> values = Lists.newArrayList(OVERRIDE_SPLITTER.split(value));

        String remarks = "";
        final Set<UsernamePassword> possibleCredentials;
        if (values.isEmpty()) {
            possibleCredentials = Collections.emptySet();
        } else {
            possibleCredentials = Sets.newLinkedHashSet();
            if (values.size() > 1) {
                final String username = values.get(0);
                final String password = values.get(1);
                if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
                    possibleCredentials.add(new UsernamePassword(username, password));
                }

                if (values.size() > 2) {
                    remarks = values.get(2);
                }
            }

            final String password = values.get(0);
            if (StringUtils.isNotEmpty(password)) {
                possibleCredentials.add(new UsernamePassword(DEFAULT_USER_1, password));
                possibleCredentials.add(new UsernamePassword(DEFAULT_USER_2, password));
            }
        }

        return new OverrideCredential(value, Collections.unmodifiableSet(possibleCredentials), remarks);
    }

    @Immutable
    public static class UsernamePassword {
        private final String username;
        private final String password;

        UsernamePassword(final String username, final String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
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

            final UsernamePassword that = (UsernamePassword) o;
            return password.equals(that.password) && username.equals(that.username);
        }

        @Override
        public int hashCode() {
            int result = username.hashCode();
            result = 31 * result + password.hashCode();
            return result;
        }
    }
}
