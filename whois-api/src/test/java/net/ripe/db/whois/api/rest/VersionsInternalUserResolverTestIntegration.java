package net.ripe.db.whois.api.rest;

import com.google.common.net.InetAddresses;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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

    @Test
    public void trusted_address_and_active_sso_session_then_internal() {
        assertThat(subject.isInternalUser("valid-token", TRUSTED), is(true));
    }

    @Test
    public void trusted_address_and_no_sso_session_then_not_internal() {
        assertThat(subject.isInternalUser(null, TRUSTED), is(false));
    }

    @Test
    public void untrusted_address_and_active_sso_session_then_not_internal() {
        assertThat(subject.isInternalUser("valid-token", UNTRUSTED), is(false));
    }

    @Test
    public void untrusted_address_and_no_sso_session_then_not_internal() {
        assertThat(subject.isInternalUser(null, UNTRUSTED), is(false));
    }

    @Test
    public void trusted_address_and_invalid_token_then_not_internal() {
        assertThat(subject.isInternalUser("invalid-token", TRUSTED), is(false));
    }
}
