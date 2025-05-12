package net.ripe.db.whois.api.rest;

import jakarta.ws.rs.core.HttpHeaders;
import net.ripe.db.whois.api.SecureRestTest;
import net.ripe.db.whois.api.httpserver.AbstractHttpsIntegrationTest;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.api.syncupdate.SyncUpdateUtils;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.acl.IpResourceConfiguration;
import net.ripe.db.whois.query.acl.SSOResourceConfiguration;
import net.ripe.db.whois.query.support.TestPersonalObjectAccounting;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import static net.ripe.db.whois.api.ApiKeyAuthServerDummy.BASIC_AUTH_PERSON_NO_MNT;
import static net.ripe.db.whois.api.ApiKeyAuthServerDummy.BASIC_AUTH_PERSON_NULL_SCOPE;
import static net.ripe.db.whois.api.rest.WhoisRestApiKeyAuthTestIntegration.getBasicAuthHeader;
import static net.ripe.db.whois.api.rest.WhoisRestBearerAuthTestIntegration.getBearerToken;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

//TODO [MA] : Delete this class once flag apikey.scope.mandatory is deprecated
@Tag("IntegrationTest")
public class WhoisRestOAuthScopeOptionalTestIntegration extends AbstractHttpsIntegrationTest {

    private static final String LOCALHOST = "127.0.0.1";
    private static final String LOCALHOST_WITH_PREFIX = "127.0.0.1/32";

    public static final String TEST_PERSON_STRING = "" +
            "person:         Test Person\n" +
            "address:        Singel 258\n" +
            "phone:          +31 6 12345678\n" +
            "nic-hdl:        TP1-TEST\n" +
            "mnt-by:         OWNER-MNT\n" +
            "source:         TEST\n";

    public static final String TEST_ROLE_STRING = "" +
            "role:          Test Role\n" +
            "address:       Singel 258\n" +
            "phone:         +31 6 12345678\n" +
            "nic-hdl:       TR2-TEST\n" +
            "e-mail:        test123@ripe.net\n" +
            "mnt-by:        OWNER-MNT\n" +
            "source:        TEST";

    public static final RpslObject TEST_PERSON = RpslObject.parse(TEST_PERSON_STRING);
    private static final RpslObject PAULETH_PALTHEN = RpslObject.parse("" +
            "person:    Pauleth Palthen\n" +
            "address:   Singel 258\n" +
            "phone:     +31-1234567890\n" +
            "e-mail:    noreply@ripe.net\n" +
            "mnt-by:    OWNER-MNT\n" +
                "nic-hdl:   PP1-TEST\n" +
            "remarks:   remark\n" +
            "source:    TEST\n");

    private static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:      OWNER-MNT\n" +
            "descr:       Owner Maintainer\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "auth:        SSO person@net.net\n" +
            "mnt-by:      OWNER-MNT\n" +
            "source:      TEST");

    private static final RpslObject TEST_ROLE = RpslObject.parse(TEST_ROLE_STRING);

    private static final RpslObject TEST_IRT = RpslObject.parse("" +
            "irt:          irt-test\n" +
            "address:      RIPE NCC\n" +
            "e-mail:       noreply@ripe.net\n" +
            "admin-c:      TP1-TEST\n" +
            "tech-c:       TP1-TEST\n" +
            "auth:         MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "mnt-by:       OWNER-MNT\n" +
            "source:       TEST\n");

    @Autowired private WhoisObjectMapper whoisObjectMapper;
    @Autowired
    private AccessControlListManager accessControlListManager;
    @Autowired
    private IpResourceConfiguration ipResourceConfiguration;
    @Autowired
    private SSOResourceConfiguration ssoResourceConfiguration;
    @Autowired
    private TestPersonalObjectAccounting testPersonalObjectAccounting;


    @BeforeAll
    public static void setupApiProperties() {
        System.setProperty("apikey.authenticate.enabled","true");
        System.setProperty("apikey.scope.mandatory","false");
    }

    @AfterAll
    public static void restApiProperties() {
        System.clearProperty("apikey.authenticate.enabled");
        System.clearProperty("apikey.scope.mandatory");
        System.clearProperty("apikey.public.key.url");
    }

    @BeforeEach
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
     //   databaseHelper.addObject("role: Test Role\nnic-hdl: TR1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TEST_PERSON);
        databaseHelper.addObject(TEST_ROLE);
        databaseHelper.addObject(TEST_IRT);
        testDateTimeProvider.setTime(LocalDateTime.parse("2001-02-04T17:00:00"));
    }

    @AfterEach
    public void reset() throws Exception {
        databaseHelper.clearAclTables();

        ipResourceConfiguration.reload();
        ssoResourceConfiguration.reload();
        testPersonalObjectAccounting.resetAccounting();
    }

    @Test
    public void create_mntner_only_data_parameter_with_apiKey_fails_null_Scope() {
        final String mntner =
                "mntner:        SSO-MNT\n" +
                        "descr:         description\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        noreply@ripe.net\n" +
                        "auth:          SSO person@net.net\n" +
                        "mnt-by:        SSO-MNT\n" +
                        "source:        TEST";

        final String response = SecureRestTest.target(getSecurePort(), "whois/syncupdates/test?" + "DATA=" + SyncUpdateUtils.encode(mntner))
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(BASIC_AUTH_PERSON_NULL_SCOPE))
                .get(String.class);

        assertThat(response, containsString("Create SUCCEEDED: [mntner] SSO-MNT"));
        assertThat(response, not(containsString("***Warning: Whois scope can not be empty")));
    }

    @Test
    public void create_mntner_only_data_parameter_with_apiKey_fails_no_mnt_Scope() {
        final String mntner =
                "mntner:        SSO-MNT\n" +
                        "descr:         description\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        noreply@ripe.net\n" +
                        "auth:          SSO person@net.net\n" +
                        "mnt-by:        SSO-MNT\n" +
                        "source:        TEST";

        final String response = SecureRestTest.target(getSecurePort(), "whois/syncupdates/test?" + "DATA=" + SyncUpdateUtils.encode(mntner))
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBearerToken(BASIC_AUTH_PERSON_NO_MNT))
                .get(String.class);

        assertThat(response, containsString("Create SUCCEEDED: [mntner] SSO-MNT"));
        assertThat(response, not(containsString("***Warning: Whois scope can not be empty")));
    }

    @Test
    public void create_mntner_only_data_parameter_with_oauth_fails_null_Scope() {
        final String mntner =
                "mntner:        SSO-MNT\n" +
                        "descr:         description\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        noreply@ripe.net\n" +
                        "auth:          SSO person@net.net\n" +
                        "mnt-by:        SSO-MNT\n" +
                        "source:        TEST";

        final String response = SecureRestTest.target(getSecurePort(), "whois/syncupdates/test?" + "DATA=" + SyncUpdateUtils.encode(mntner))
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_NULL_SCOPE))
                .get(String.class);

        assertThat(response, containsString("Create SUCCEEDED: [mntner] SSO-MNT"));
        assertThat(response, not(containsString("***Warning: Whois scope can not be empty")));
    }

    @Test
    public void create_mntner_only_data_parameter_with_oauth_fails_no_mnt_Scope() {
        final String mntner =
                "mntner:        SSO-MNT\n" +
                        "descr:         description\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        noreply@ripe.net\n" +
                        "auth:          SSO person@net.net\n" +
                        "mnt-by:        SSO-MNT\n" +
                        "source:        TEST";

        final String response = SecureRestTest.target(getSecurePort(), "whois/syncupdates/test?" + "DATA=" + SyncUpdateUtils.encode(mntner))
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_NO_MNT))
                .get(String.class);

        assertThat(response, containsString("Create SUCCEEDED: [mntner] SSO-MNT"));
        assertThat(response, not(containsString("***Warning: Whois scope can not be empty")));
    }

}
