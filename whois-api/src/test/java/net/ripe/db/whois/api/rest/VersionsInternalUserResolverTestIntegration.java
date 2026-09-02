package net.ripe.db.whois.api.rest;

import com.google.common.net.InetAddresses;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.AfterAll;
import org.springframework.beans.factory.annotation.Autowired;
import java.net.InetAddress;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Tag("IntegrationTest")
public class VersionsInternalUserResolverTestIntegration extends AbstractIntegrationTest {
    private static final InetAddress TRUSTED = InetAddresses.forString("127.0.0.1");
    private static final InetAddress UNTRUSTED = InetAddresses.forString("193.0.0.1");

    @Autowired
    private VersionsInternalUserResolver subject;


    @BeforeAll
    public static void beforeClass() {
        System.setProperty("versions.internal.emails", "person@net.net,test@ripe.net");
    }

    @AfterAll
    public static void afterClass() {
        System.clearProperty("versions.internal.emails");
    }

    @Test
    public void trusted_address_and_allowed_email_then_internal() {
        assertThat(subject.isInternalUser("valid-token", TRUSTED), is(true));
    }

    @Test
    public void trusted_address_and_email_not_in_allow_list_then_not_internal() {
        assertThat(subject.isInternalUser("person2", TRUSTED), is(false));
    }

    @Test
    public void untrusted_address_and_allowed_email_then_not_internal() {
        assertThat(subject.isInternalUser("valid-token", UNTRUSTED), is(false));
    }

    @Test
    public void untrusted_address_and_no_sso_session_then_not_internal() {
        assertThat(subject.isInternalUser(null, UNTRUSTED), is(false));
    }

    @Test
    public void multiple_allow_list_entries_each_match() {
        assertThat(subject.isInternalUser("valid-token", TRUSTED), is(true));
        assertThat(subject.isInternalUser("test@ripe.net", TRUSTED), is(true));
        assertThat(subject.isInternalUser("person2@ripe.net", TRUSTED), is(false));
    }

    @Test
    public void trusted_address_and_invalid_token_then_not_internal() {
        assertThat(subject.isInternalUser("invalid-token", TRUSTED), is(false));
    }

    @Test
    public void trusted_address_and_missing_token_then_not_internal() {
        assertThat(subject.isInternalUser(null, TRUSTED), is(false));
    }
}