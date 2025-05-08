package net.ripe.db.whois.update.domain;

import net.ripe.db.whois.common.Credentials.OverrideCredential;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class OverrideCredentialTest {
    final Optional<OverrideCredential.OverrideValues> absent = Optional.empty();

    @Test
    public void parse_empty() {
        final OverrideCredential overrideCredential = OverrideCredential.parse("");

        assertThat(overrideCredential.toString(), is("OverrideCredential{NOT_VALID}"));

        final Optional<OverrideCredential.OverrideValues> possibleCredentials = overrideCredential.getOverrideValues();
        assertThat(possibleCredentials, is(absent));
    }

    @Test
    public void parse_one_value() {
        final OverrideCredential overrideCredential = OverrideCredential.parse("password");

        assertThat(overrideCredential.getOverrideValues(), is(absent));
    }

    @Test
    public void parse_two_values() {
        final OverrideCredential overrideCredential = OverrideCredential.parse("user,password");

        assertThat(overrideCredential.toString(), is("OverrideCredential{user,FILTERED}"));

        final Optional<OverrideCredential.OverrideValues> possibleCredentials = overrideCredential.getOverrideValues();
        assertThat(possibleCredentials, not(is(absent)));
        assertThat(possibleCredentials.get(), is(new OverrideCredential.OverrideValues("user", "password", "")));
    }

    @Test
    public void parse_three_values() {
        final OverrideCredential overrideCredential = OverrideCredential.parse("user,password,remarks");

        assertThat(overrideCredential.toString(), is("OverrideCredential{user,FILTERED,remarks}"));

        final Optional<OverrideCredential.OverrideValues> overrideValues = overrideCredential.getOverrideValues();
        assertThat(overrideValues, not(is(absent)));
        assertThat(overrideValues.get(), is(new OverrideCredential.OverrideValues("user", "password", "remarks")));
    }

    @Test
    public void parse_more_values() {
        final OverrideCredential overrideCredential = OverrideCredential.parse("user,password,remarks, and some more");

        assertThat(overrideCredential.toString(), is("OverrideCredential{user,FILTERED,remarks, and some more}"));

        final Optional<OverrideCredential.OverrideValues> possibleCredentials = overrideCredential.getOverrideValues();
        assertThat(possibleCredentials, not(is(absent)));
        assertThat(possibleCredentials.get(), is(new OverrideCredential.OverrideValues("user", "password", "remarks, and some more")));
    }

    @Test
    public void equal() {
        final OverrideCredential overrideCredential = OverrideCredential.parse("user,password,remarks");

        assertThat(overrideCredential.equals(null), is(false));
        assertThat(overrideCredential.equals(""), is(false));
        assertThat(overrideCredential.equals(overrideCredential), is(true));
        assertThat(overrideCredential.equals(OverrideCredential.parse("user,password,remarks")), is(true));
        assertThat(overrideCredential.equals(OverrideCredential.parse("USER,password,remarks")), is(false));
    }

    @Test
    public void hash() {
        assertThat(OverrideCredential.parse("user,password,remarks").hashCode(), is(OverrideCredential.parse("user,password,remarks").hashCode()));
    }

    @Test
    public void testToString() {
        assertThat(OverrideCredential.parse("").toString(), is("OverrideCredential{NOT_VALID}"));
        assertThat(OverrideCredential.parse("user").toString(), is("OverrideCredential{NOT_VALID}"));
        assertThat(OverrideCredential.parse("user,password").toString(), is("OverrideCredential{user,FILTERED}"));
        assertThat(OverrideCredential.parse("user, password").toString(), is("OverrideCredential{user,FILTERED}"));
        assertThat(OverrideCredential.parse("user, password, remarks").toString(), is("OverrideCredential{user,FILTERED,remarks}"));
        assertThat(OverrideCredential.parse("user,password,remarks,more remarks").toString(), is("OverrideCredential{user,FILTERED,remarks,more remarks}"));
    }
}
