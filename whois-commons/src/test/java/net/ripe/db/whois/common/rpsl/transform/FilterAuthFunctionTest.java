package net.ripe.db.whois.common.rpsl.transform;

import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.sso.CrowdClient;
import net.ripe.db.whois.common.sso.CrowdClientException;
import net.ripe.db.whois.common.sso.SsoTokenTranslator;
import net.ripe.db.whois.common.sso.UserSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FilterAuthFunctionTest {

    @Mock
    private SsoTokenTranslator ssoTokenTranslator;
    @Mock
    private CrowdClient crowdClient;
    @Mock
    private RpslObjectDao rpslObjectDao;

    private FilterAuthFunction subject;

    @Before
    public void setUp() throws Exception {
        subject = new FilterAuthFunction();
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
        subject = new FilterAuthFunction(Collections.singletonList("test0"), null, ssoTokenTranslator, crowdClient, rpslObjectDao);
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
        subject = new FilterAuthFunction(Collections.singletonList("test1"), null, ssoTokenTranslator, crowdClient, rpslObjectDao);
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
        final UserSession userSession = new UserSession("noreply@ripe.net", true);
        userSession.setUuid("76cab38b73eb-ac91-4336-94f3-d06e5500");
        when(ssoTokenTranslator.translateSsoToken("token")).thenReturn(userSession);
        when(crowdClient.getUsername("d06e5500-ac91-4336-94f3-76cab38b73eb")).thenReturn("user@host.org");

        final RpslObject rpslObject = RpslObject.parse("" +
                "mntner: SSO-MNT\n" +
                "auth: SSO d06e5500-ac91-4336-94f3-76cab38b73eb\n" +
                "source: RIPE");

        subject = new FilterAuthFunction(Collections.<String>emptyList(), "token", ssoTokenTranslator, crowdClient, rpslObjectDao);
        final RpslObject result = subject.apply(rpslObject);

        assertThat(result.toString(), is(
                "mntner:         SSO-MNT\n" +
                "auth:           SSO # Filtered\n" +
                "source:         RIPE # Filtered\n"));
    }

    @Test
    public void apply_sso_unfiltered() {
        final UserSession userSession = new UserSession("user@host.org", true);
        userSession.setUuid("d06e5500-ac91-4336-94f3-76cab38b73eb");
        when(ssoTokenTranslator.translateSsoToken("token")).thenReturn(userSession);
        when(crowdClient.getUsername("d06e5500-ac91-4336-94f3-76cab38b73eb")).thenReturn("user@host.org");

        final RpslObject rpslObject = RpslObject.parse("" +
                "mntner: SSO-MNT\n" +
                "auth: SSO d06e5500-ac91-4336-94f3-76cab38b73eb\n" +
                "source: RIPE");

        subject = new FilterAuthFunction(Collections.<String>emptyList(), "token", ssoTokenTranslator, crowdClient, rpslObjectDao);
        final RpslObject result = subject.apply(rpslObject);

        assertThat(result.toString(), is(
                "mntner:         SSO-MNT\n" +
                "auth:           SSO user@host.org\n" +
                "source:         RIPE\n"));
    }

    @Test(expected = CrowdClientException.class)
    public void crowd_client_exception() throws Exception {
        final UserSession userSession = new UserSession("user@host.org", true);
        userSession.setUuid("d06e5500-ac91-4336-94f3-76cab38b73eb");

        when(ssoTokenTranslator.translateSsoToken("token")).thenReturn(userSession);
        when(crowdClient.getUsername("d06e5500-ac91-4336-94f3-76cab38b73eb")).thenThrow(CrowdClientException.class);

        subject = new FilterAuthFunction(Collections.<String>emptyList(), "token", ssoTokenTranslator, crowdClient, rpslObjectDao);
        subject.apply(RpslObject.parse("" +
                "mntner: SSO-MNT\n" +
                "auth: SSO d06e5500-ac91-4336-94f3-76cab38b73eb\n" +
                "source: RIPE"));
    }

    @Test
    public void crowd_client_exception_server_down() throws Exception {
        final UserSession userSession = new UserSession("user@host.org", true);
        userSession.setUuid("T2hOz8tlmka5lxoZQxzC1Q00");

        when(ssoTokenTranslator.translateSsoToken("token")).thenThrow(CrowdClientException.class);
        when(crowdClient.getUsername("T2hOz8tlmka5lxoZQxzC1Q00")).thenThrow(CrowdClientException.class);

        subject = new FilterAuthFunction(Collections.<String>emptyList(), "token", ssoTokenTranslator, crowdClient, rpslObjectDao);
        final RpslObject result = subject.apply(
                RpslObject.parse("" +
                        "mntner: SSO-MNT\n" +
                        "auth: SSO T2hOz8tlmka5lxoZQxzC1Q00\n" +
                        "source: RIPE"));

        assertThat(result.toString(), is("" +
                "mntner:         SSO-MNT\n" +
                "auth:           SSO # Filtered\n" +
                "source:         RIPE # Filtered\n"));
    }

    @Test
    public void sso_token_translator_exception() throws Exception {
        when(ssoTokenTranslator.translateSsoToken(any(String.class))).thenThrow(CrowdClientException.class);
        subject = new FilterAuthFunction(Collections.<String>emptyList(), "token", ssoTokenTranslator, crowdClient, rpslObjectDao);

        final RpslObject result = subject.apply(
                RpslObject.parse("" +
                        "mntner: SSO-MNT\n" +
                        "auth: SSO T2hOz8tlmka5lxoZQxzC1Q00\n" +
                        "source: RIPE"));

        assertThat(result.toString(), is("" +
                "mntner:         SSO-MNT\n" +
                "auth:           SSO # Filtered\n" +
                "source:         RIPE # Filtered\n"));
    }
}
