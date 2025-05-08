package net.ripe.db.whois.common.credentials;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Immutable
public final class OverrideCredential implements Credential {
    private static final Splitter OVERRIDE_SPLITTER = Splitter.on(',').trimResults().limit(3);

    private final String value;
    private final Optional<OverrideValues> overrideValues;

    private OverrideCredential(final String value, final Optional<OverrideValues> overrideValues) {
        this.value = value;
        this.overrideValues = overrideValues;
    }

    public Optional<OverrideValues> getOverrideValues() {
        return overrideValues;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof OverrideCredential)) return false;
        final OverrideCredential that = (OverrideCredential) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        if (overrideValues.isEmpty()){
            return "OverrideCredential{NOT_VALID}";
        }

        if (StringUtils.isBlank(overrideValues.get().getRemarks())){
            return String.format("OverrideCredential{%s,FILTERED}", overrideValues.get().getUsername());
        }

        return String.format("OverrideCredential{%s,FILTERED,%s}",
                overrideValues.get().getUsername(), overrideValues.get().getRemarks());
    }

    public static OverrideCredential parse(final String value) {
        final List<String> values = Lists.newArrayList(OVERRIDE_SPLITTER.split(value));

        final OverrideCredential notValidCredentials = new OverrideCredential(value, Optional.empty());

        if (values.size() < 2) {
            return notValidCredentials;
        }

        final String username = values.get(0);
        final String password = values.get(1);
        final String remarks = values.size() > 2 ? values.get(2) : "";

        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            return notValidCredentials;
        }

        return new OverrideCredential(value, Optional.of(new OverrideValues(username, password, remarks)));
    }

    @Immutable
    public static class OverrideValues {
        private final String username;
        private final String password;
        private final String remarks;

        public OverrideValues(final String username, final String password, final String remarks) {
            this.username = username;
            this.password = password;
            this.remarks = remarks;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getRemarks() {
            return remarks;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!(o instanceof OverrideValues)) return false;
            final OverrideValues that = (OverrideValues) o;
            return Objects.equals(username, that.username) &&
                    Objects.equals(password, that.password) &&
                    Objects.equals(remarks, that.remarks);
        }

        @Override
        public int hashCode() {
            return Objects.hash(username, password, remarks);
        }
    }
}
