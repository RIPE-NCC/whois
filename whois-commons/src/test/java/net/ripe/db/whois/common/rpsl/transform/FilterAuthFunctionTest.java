package net.ripe.db.whois.common.rpsl.transform;

import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.override.OverrideCredentialValidator;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.sso.AuthServiceClient;
import net.ripe.db.whois.common.sso.AuthServiceClientException;
import net.ripe.db.whois.common.sso.SsoTokenTranslator;
import net.ripe.db.whois.common.sso.UserSession;
import net.ripe.db.whois.common.x509.ClientAuthCertificateValidator;
import org.apache.commons.compress.utils.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FilterAuthFunctionTest {

    @Mock
    private SsoTokenTranslator ssoTokenTranslator;

    @Mock
    private AuthServiceClient authServiceClient;
    @Mock
    private RpslObjectDao rpslObjectDao;

    @Mock
    private ClientAuthCertificateValidator clientAuthCertificateValidator;

    @Mock
    private OverrideCredentialValidator overrideCredentialValidator;

    private FilterAuthFunction subject;

    @BeforeEach
    public void setUp() throws Exception {
        subject = new FilterAuthFunction(Lists.newArrayList(), null, null, null, authServiceClient,
                rpslObjectDao, Lists.newArrayList(), clientAuthCertificateValidator, overrideCredentialValidator,
                false);

    }

    @Test
    public void apply_irt() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "irt: DEV-IRT\n" +
                "auth: MD5-PW $1$YmPozTxJ$s3eGZRVrKVGdSDTeEZJu\n" +
                "source: RIPE"
        );

        final RpslObject response = subject.apply(rpslObject);
        assertThat(response, is(RpslObject.parse("" +
                "irt:            DEV-IRT\n" +
                "auth:           MD5-PW # Filtered\n" +
                "source:         RIPE # Filtered\n")));
    }

    @Test
    public void apply_no_md5() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "mntner: WEIRD-MNT\n" +
                "auth: value\n" +
                "source: RIPE"
        );

        final RpslObject response = subject.apply(rpslObject);
        assertThat(response, is(rpslObject));
    }

    @Test
    public void apply_md5_filtered() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "mntner: WEIRD-MNT\n" +
                "auth: MD5-PW $1$YmPozTxJ$s3eGZRVrKVGdSDTeEZJu//\n" +
                "auth: MD5-PW $1$YmPozTxJ$s3eGZRVrKVGdSDTeEZJu//\n" +
                "source: RIPE"
        );

        final RpslObject response = subject.apply(rpslObject);

        assertThat(response.toString(), is("" +
                "mntner:         WEIRD-MNT\n" +
                "auth:           MD5-PW # Filtered\n" +
                "auth:           MD5-PW # Filtered\n" +
                "source:         RIPE # Filtered\n"));
    }

    @Test
    public void apply_md5_filtered_incorrect_password() {
        subject = new FilterAuthFunction(Collections.singletonList("test0"), null, null, null, authServiceClient,
                rpslObjectDao, Lists.newArrayList(), clientAuthCertificateValidator, overrideCredentialValidator,
                false);
        final RpslObject rpslObject = RpslObject.parse("" +
                "mntner:         WEIRD-MNT\n" +
                "auth:           MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:           MD5-PW $1$5XCg9Q1W$O7g9bgeJPkpea2CkBGnz/0 #test1\n" +
                "auth:           MD5-PW $1$ZjlXZmWO$VKyuYp146Vx5b1.398zgH/ #test2\n" +
                "source:         RIPE"
        );

        final RpslObject response = subject.apply(rpslObject);

        assertThat(response.toString(), is("" +
                "mntner:         WEIRD-MNT\n" +
                "auth:           MD5-PW # Filtered\n" +
                "auth:           MD5-PW # Filtered\n" +
                "auth:           MD5-PW # Filtered\n" +
                "source:         RIPE # Filtered\n"));
    }

    @Test
    public void apply_md5_unfiltered() {
        subject = new FilterAuthFunction(Collections.singletonList("test1"), null, null, null, authServiceClient,
                rpslObjectDao, Lists.newArrayList(), clientAuthCertificateValidator, overrideCredentialValidator,
                false);
        final RpslObject rpslObject = RpslObject.parse("" +
                "mntner:         WEIRD-MNT\n" +
                "auth:           MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:           MD5-PW $1$5XCg9Q1W$O7g9bgeJPkpea2CkBGnz/0 #test1\n" +
                "auth:           MD5-PW $1$ZjlXZmWO$VKyuYp146Vx5b1.398zgH/ #test2\n" +
                "source:         RIPE"
        );

        final RpslObject response = subject.apply(rpslObject);

        assertThat(response.toString(), is("" +
                "mntner:         WEIRD-MNT\n" +
                "auth:           MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:           MD5-PW $1$5XCg9Q1W$O7g9bgeJPkpea2CkBGnz/0 #test1\n" +
                "auth:           MD5-PW $1$ZjlXZmWO$VKyuYp146Vx5b1.398zgH/ #test2\n" +
                "source:         RIPE\n"));
    }

    @Test
    public void apply_sso_filtered() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "mntner: SSO-MNT\n" +
                "auth: SSO T2hOz8tlmka5lxoZQxzC1Q00\n" +
                "source: RIPE");

        final RpslObject result = subject.apply(rpslObject);

        assertThat(result.toString(), is("" +
                "mntner:         SSO-MNT\n" +
                "auth:           SSO # Filtered\n" +
                "source:         RIPE # Filtered\n"));
    }

    @Test
    public void apply_sso_different_uuid_filtered() {
        final UserSession userSession = new UserSession("76cab38b73eb-ac91-4336-94f3-d06e5500","noreply@ripe.net", "Test User", true, "2033-01-30T16:38:27.369+11:00");

        final RpslObject rpslObject = RpslObject.parse("" +
                "mntner: SSO-MNT\n" +
                "auth: SSO d06e5500-ac91-4336-94f3-76cab38b73eb\n" +
                "source: RIPE");

        subject = new FilterAuthFunction(Collections.<String>emptyList(), null, null, userSession, authServiceClient,
                rpslObjectDao, Lists.newArrayList(), clientAuthCertificateValidator, overrideCredentialValidator, false);
        final RpslObject result = subject.apply(rpslObject);

        assertThat(result.toString(), is(
                "mntner:         SSO-MNT\n" +
                "auth:           SSO # Filtered\n" +
                "source:         RIPE # Filtered\n"));
    }

    @Test
    public void apply_sso_unfiltered() {
        final UserSession userSession = new UserSession("d06e5500-ac91-4336-94f3-76cab38b73eb","user@host.org", "Test User", true, "2033-01-30T16:38:27.369+11:00");
        when(authServiceClient.getUsername("d06e5500-ac91-4336-94f3-76cab38b73eb")).thenReturn("user@host.org");

        final RpslObject rpslObject = RpslObject.parse("" +
                "mntner: SSO-MNT\n" +
                "auth: SSO d06e5500-ac91-4336-94f3-76cab38b73eb\n" +
                "source: RIPE");

        subject = new FilterAuthFunction(Collections.<String>emptyList(), null, null, userSession, authServiceClient,
                rpslObjectDao, Lists.newArrayList(), clientAuthCertificateValidator, overrideCredentialValidator, false);
        final RpslObject result = subject.apply(rpslObject);

        assertThat(result.toString(), is(
                "mntner:         SSO-MNT\n" +
                "auth:           SSO user@host.org\n" +
                "source:         RIPE\n"));
    }

    @Test
    public void crowd_client_exception() {
        assertThrows(AuthServiceClientException.class, () -> {
            final UserSession userSession = new UserSession("d06e5500-ac91-4336-94f3-76cab38b73eb","user@host.org", "Test User", true, "2033-01-30T16:38:27.369+11:00");

            when(authServiceClient.getUsername("d06e5500-ac91-4336-94f3-76cab38b73eb")).thenThrow(AuthServiceClientException.class);

            subject = new FilterAuthFunction(Collections.<String>emptyList(), null, null, userSession,
                    authServiceClient, rpslObjectDao, Lists.newArrayList(), clientAuthCertificateValidator,
                    overrideCredentialValidator, false);
            subject.apply(RpslObject.parse("" +
                    "mntner: SSO-MNT\n" +
                    "auth: SSO d06e5500-ac91-4336-94f3-76cab38b73eb\n" +
                    "source: RIPE"));
        });
    }
}
