package net.ripe.db.whois.api.rest;

import com.google.common.net.InetAddresses;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.sso.SsoTokenTranslator;
import net.ripe.db.whois.common.sso.UserSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import java.net.InetAddress;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class VersionsInternalUserResolverTest {
    private static final InetAddress ADDRESS = InetAddresses.forString("10.0.0.1");
    private static final String TOKEN = "token";
    private static final String ALLOWED_EMAIL = "alice@ripe.net";
    private static final String OTHER_EMAIL = "mallory@ripe.net";

    @Mock private SsoTokenTranslator ssoTokenTranslator;
    @Mock private IpRanges ipRanges;

    private VersionsInternalUserResolver subject;

    @BeforeEach
    public void setup() {
        subject = new VersionsInternalUserResolver(ssoTokenTranslator, ipRanges, ALLOWED_EMAIL);
    }

    private void trusted(final boolean trusted) {
        when(ipRanges.isTrusted(any(Ipv4Resource.class))).thenReturn(trusted);
    }

    private void ssoUser(final String username) {
        when(ssoTokenTranslator.translateSsoTokenOrNull(TOKEN))
                .thenReturn(new UserSession("uuid", username, "Test User", true, null));
    }

    @Test
    public void trusted_address_and_allowed_email_then_internal() {
        trusted(true);
        ssoUser(ALLOWED_EMAIL);

        assertThat(subject.isInternalUser(TOKEN, ADDRESS), is(true));
    }

    @Test
    public void trusted_address_and_email_not_in_allow_list_then_not_internal() {
        trusted(true);
        ssoUser(OTHER_EMAIL);

        assertThat(subject.isInternalUser(TOKEN, ADDRESS), is(false));
    }

    @Test
    public void untrusted_address_and_allowed_email_then_not_internal() {
        trusted(false);
        ssoUser(ALLOWED_EMAIL);

        assertThat(subject.isInternalUser(TOKEN, ADDRESS), is(false));
    }

    @Test
    public void untrusted_address_and_no_sso_session_then_not_internal() {
        trusted(false);

        assertThat(subject.isInternalUser(null, ADDRESS), is(false));
    }

    @Test
    public void untrusted_address_skips_sso_lookup() {
        trusted(false);

        subject.isInternalUser(TOKEN, ADDRESS);

        verifyNoInteractions(ssoTokenTranslator);
    }

    @Test
    public void trusted_address_and_missing_token_then_not_internal() {
        trusted(true);

        assertThat(subject.isInternalUser(null, ADDRESS), is(false));
        assertThat(subject.isInternalUser("", ADDRESS), is(false));
    }

    @Test
    public void trusted_address_and_invalid_token_then_not_internal() {
        trusted(true);
        when(ssoTokenTranslator.translateSsoTokenOrNull(TOKEN)).thenReturn(null);

        assertThat(subject.isInternalUser(TOKEN, ADDRESS), is(false));
    }

    @Test
    public void trusted_address_and_inactive_session_then_not_internal() {
        trusted(true);
        when(ssoTokenTranslator.translateSsoTokenOrNull(TOKEN))
                .thenReturn(new UserSession("uuid", ALLOWED_EMAIL, "Test User", false, null));

        assertThat(subject.isInternalUser(TOKEN, ADDRESS), is(false));
    }

    @Test
    public void trusted_address_and_session_without_username_then_not_internal() {
        trusted(true);
        ssoUser(null);

        assertThat(subject.isInternalUser(TOKEN, ADDRESS), is(false));
    }

    @Test
    public void email_comparison_is_case_insensitive() {
        trusted(true);
        ssoUser("Alice@RIPE.NET");

        assertThat(subject.isInternalUser(TOKEN, ADDRESS), is(true));
    }

    @Test
    public void allow_list_entries_are_trimmed_and_lowercased() {
        subject = new VersionsInternalUserResolver(ssoTokenTranslator, ipRanges, "  Alice@RIPE.NET  ");
        trusted(true);
        ssoUser(ALLOWED_EMAIL);

        assertThat(subject.isInternalUser(TOKEN, ADDRESS), is(true));
    }

    @Test
    public void blank_allow_list_entries_are_ignored() {
        subject = new VersionsInternalUserResolver(ssoTokenTranslator, ipRanges, " ", "", ALLOWED_EMAIL);
        trusted(true);
        ssoUser(ALLOWED_EMAIL);

        assertThat(subject.isInternalUser(TOKEN, ADDRESS), is(true));
    }

    @Test
    public void empty_allow_list_then_nobody_is_internal() {
        subject = new VersionsInternalUserResolver(ssoTokenTranslator, ipRanges);
        trusted(true);
        ssoUser(ALLOWED_EMAIL);

        assertThat(subject.isInternalUser(TOKEN, ADDRESS), is(false));
    }
}
