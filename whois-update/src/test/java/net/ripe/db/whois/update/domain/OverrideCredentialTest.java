package net.ripe.db.whois.update.domain;

import org.junit.Test;

import java.util.Set;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class OverrideCredentialTest {
    @Test
    public void parse_empty() {
        final OverrideCredential overrideCredential = OverrideCredential.parse("");

        assertThat(overrideCredential.toString(), is(""));

        final Set<OverrideCredential.UsernamePassword> possibleCredentials = overrideCredential.getPossibleCredentials();
        assertThat(possibleCredentials, hasSize(0));
    }

    @Test
    public void parse_one_value() {
        final OverrideCredential overrideCredential = OverrideCredential.parse("password");

        assertTrue(overrideCredential.getPossibleCredentials().isEmpty());
    }

    @Test
    public void parse_two_values() {
        final OverrideCredential overrideCredential = OverrideCredential.parse("user,password");

        assertThat(overrideCredential.toString(), is("user,password"));

        final Set<OverrideCredential.UsernamePassword> possibleCredentials = overrideCredential.getPossibleCredentials();
        assertThat(possibleCredentials, hasSize(1));
        assertThat(possibleCredentials, containsInAnyOrder(new OverrideCredential.UsernamePassword("user", "password")));

        assertThat(overrideCredential.getRemarks(), is(""));
    }

    @Test
    public void parse_three_values() {
        final OverrideCredential overrideCredential = OverrideCredential.parse("user,password,remarks");

        assertThat(overrideCredential.toString(), is("user,password,remarks"));

        final Set<OverrideCredential.UsernamePassword> possibleCredentials = overrideCredential.getPossibleCredentials();
        assertThat(possibleCredentials, hasSize(1));
        assertThat(possibleCredentials, containsInAnyOrder(new OverrideCredential.UsernamePassword("user", "password")));

        assertThat(overrideCredential.getRemarks(), is("remarks"));
    }

    @Test
    public void parse_more_values() {
        final OverrideCredential overrideCredential = OverrideCredential.parse("user,password,remarks, and some more");

        assertThat(overrideCredential.toString(), is("user,password,remarks, and some more"));

        final Set<OverrideCredential.UsernamePassword> possibleCredentials = overrideCredential.getPossibleCredentials();
        assertThat(possibleCredentials, hasSize(1));
        assertThat(possibleCredentials, containsInAnyOrder(new OverrideCredential.UsernamePassword("user", "password")));

        assertThat(overrideCredential.getRemarks(), is("remarks, and some more"));
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
}
