package net.ripe.db.whois.api.rest;

import jakarta.ws.rs.core.HttpHeaders;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.SecureRestTest;
import net.ripe.db.whois.api.httpserver.AbstractHttpsIntegrationTest;
import net.ripe.db.whois.api.syncupdate.SyncUpdateUtils;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static net.ripe.db.whois.api.ApiKeyAuthServerDummy.BASIC_AUTH_PERSON_ANY_MNT;
import static net.ripe.db.whois.api.rest.WhoisRestApiKeyAuthTestIntegration.getBasicAuthHeader;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@Tag("IntegrationTest")
public class SyncUpdatesServicePasswdNotSupportedTestIntegration extends AbstractHttpsIntegrationTest {

    public static final String TEST_PERSON_STRING = """
            person:         Test Person
            address:        Singel 258
            phone:          +31 6 12345678
            nic-hdl:        TP1-TEST
            mnt-by:         OWNER-MNT
            source:         TEST
            """;

    private static final String OWNER_MNT = """
            mntner:      OWNER-MNT
            descr:       Owner Maintainer
            admin-c:     TP1-TEST
            upd-to:      noreply@ripe.net
            auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test
            auth:        SSO person@net.net
            mnt-by:      OWNER-MNT
            source:      TEST
            """;

    private static final String IRT = """
            irt:          irt-test
            address:      RIPE NCC
            e-mail:       irt-dbtest@ripe.net
            auth:         MD5-PW $1$qxm985sj$3OOxndKKw/fgUeQO7baeF/  #irt
            auth:         SSO person@net.net
            irt-nfy:      irt_nfy1_dbtest@ripe.net
            notify:       nfy_dbtest@ripe.net
            admin-c:      TP1-TEST
            tech-c:       TP1-TEST
            mnt-by:       OWNER-MNT
            source:       TEST
            """;

    private static final String INETNUM = """
            inetnum:      192.168.0.0 - 192.168.255.255
            netname:      RIPE-NET1
            country:      NL
            admin-c:      TP1-TEST
            tech-c:       TP1-TEST
            status:       ALLOCATED PA
            mnt-by:       OWNER-MNT
            source:       TEST
            """;

    @BeforeAll
    public static void setUp() {
        System.setProperty("md5.password.supported", "false");
        System.setProperty("irt.password.supported", "false");

        System.setProperty("apikey.authenticate.enabled","true");
        System.setProperty("apikey.max.scope","2");
    }

    @AfterAll
    public static void clearProperties() {
        System.clearProperty("md5.password.supported");
        System.clearProperty("irt.password.supported");

        System.clearProperty("apikey.authenticate.enabled");
        System.clearProperty("apikey.max.scope");
    }

    @BeforeEach
    public void setup() {
        databaseHelper.addObject("inetnum: 0.0.0.0 - 255.255.255.255\nstatus: ALLOCATED UNSPECIFIED\nsource: TEST");
        databaseHelper.addObjects(RpslObject.parse(TEST_PERSON_STRING),
                RpslObject.parse(OWNER_MNT),
                RpslObject.parse(IRT),
                RpslObject.parse(INETNUM));
        ipTreeUpdater.rebuild();
    }

    // POST
    @Test
    public void create_person_with_password_error() {

        final String createPerson = """
                person:    Pauleth Palthen
                address:   Singel 258
                phone:     +31-1234567890
                e-mail:    noreply@ripe.net
                mnt-by:    OWNER-MNT
                nic-hdl:   PP1-TEST
                remarks:   remark
                source:    TEST
                """;

        final String errorString = RestTest.target(getPort(), "whois/syncupdates/test?" +
                        "DATA=" + SyncUpdateUtils.encode(createPerson + "\npassword: test"))
                .request()
                .get(String.class);

        assertThat(errorString, containsString("Number of objects processed successfully:  0"));
        assertThat(errorString, containsString("""
            ***Error:   Authorisation for [person] PP1-TEST failed
                        using "mnt-by:"
                        not authenticated by: OWNER-MNT"""));
        assertThat(errorString, containsString("""
            ***Warning: MD5 hashed password authentication has been ignored because is not
                        longer supported."""));

    }

    @Test
    public void create_new_mntner_with_password_error() {

        final String createMd5Mntner = """
                mntner:      MD5-MNT
                descr:       Owner Maintainer
                admin-c:     TP1-TEST
                upd-to:      noreply@ripe.net
                auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test
                auth:        SSO person@net.net
                mnt-by:      MD5-MNT
                source:      TEST
                """;

        final String errorString = RestTest.target(getPort(), "whois/syncupdates/test?" +
                        "DATA=" + SyncUpdateUtils.encode(createMd5Mntner + "\npassword: test"))
                .request()
                .get(String.class);

        assertThat(errorString, containsString("Number of objects processed successfully:  0"));
        assertThat(errorString, containsString("""
                ***Error:   Authorisation for [mntner] MD5-MNT failed
                            using "mnt-by:"
                            not authenticated by: MD5-MNT"""));
        assertThat(errorString, containsString("""
                ***Error:   MD5 hashed password authentication is deprecated. Please switch to
                            an alternative authentication method.
                """));
        assertThat(errorString, containsString("""
            ***Warning: MD5 hashed password authentication has been ignored because is not
                        longer supported."""));
    }

    @Test
    public void create_new_password_mntner_with_apikeys_error() {

        final String createMd5Mntner = """
                mntner:      MD5-MNT
                descr:       Owner Maintainer
                admin-c:     TP1-TEST
                upd-to:      noreply@ripe.net
                auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test
                auth:        SSO person@net.net
                mnt-by:      MD5-MNT
                source:      TEST
                """;

        final String errorString = SecureRestTest.target(getSecurePort(), "whois/syncupdates/test?" +
                        "DATA=" + SyncUpdateUtils.encode(createMd5Mntner))
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                .get(String.class);

        assertThat(errorString, containsString("Number of objects processed successfully:  0"));
        assertThat(errorString, containsString("""
                ***Error:   MD5 hashed password authentication is deprecated. Please switch to
                            an alternative authentication method.
                """));
    }

    @Test
    public void create_new_sso_mntner_with_apikeys_succeed() {

        final String createMd5Mntner = """
                mntner:      MD5-MNT
                descr:       Owner Maintainer
                admin-c:     TP1-TEST
                upd-to:      noreply@ripe.net
                auth:        SSO test@ripe.net
                auth:        SSO person@net.net
                mnt-by:      MD5-MNT
                source:      TEST
                """;

        final String succeeded = SecureRestTest.target(getSecurePort(), "whois/syncupdates/test?" +
                        "DATA=" + SyncUpdateUtils.encode(createMd5Mntner))
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                .get(String.class);

        assertThat(succeeded, containsString("Number of objects processed successfully:  1"));
    }

    @Test
    public void create_person_with_crowd_succeed() {

        final String createPerson = """
                person:    Pauleth Palthen
                address:   Singel 258
                phone:     +31161715123
                mnt-by:    OWNER-MNT
                nic-hdl:   PP1-TEST
                source:    TEST
                """;

        final String succeeded = SecureRestTest.target(getSecurePort(), "whois/syncupdates/test?" + "DATA=" + SyncUpdateUtils.encode(createPerson))
                .request()
                .cookie("crowd.token_key", "valid-token")
                .get(String.class);

        assertThat(succeeded, containsString("Number of objects processed successfully:  1"));
    }

    @Test
    public void create_person_with_both_crowd_and_password_succeed() {

        final String createPerson = """
                person:    Pauleth Palthen
                address:   Singel 258
                phone:     +31-1234567890
                e-mail:    noreply@ripe.net
                mnt-by:    OWNER-MNT
                nic-hdl:   PP1-TEST
                remarks:   remark
                source:    TEST
                """;

        final String succeeded = SecureRestTest.target(getSecurePort(), "whois/syncupdates/test?"  + "DATA=" + SyncUpdateUtils.encode(createPerson))
                .request()
                .cookie("crowd.token_key", "valid-token")
                .get(String.class);


        assertThat(succeeded, containsString("Number of objects processed successfully:  1"));
    }

    @Test
    public void create_new_resource_with_irt_password_error() {

        final String createResource = """
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       OWNER-MNT
                mnt-irt:      irt-test
                source:       TEST
                """;

        final String errorString = SecureRestTest.target(getSecurePort(), "whois/syncupdates/test?" +
                        "DATA=" + SyncUpdateUtils.encode(createResource + "\npassword: irt"))
                .request()
                .get(String.class);

        assertThat(errorString, containsString("Number of objects processed successfully:  0"));
        assertThat(errorString, containsString("""
                ***Warning: MD5 hashed password authentication has been ignored because is not
                            longer supported.
                """));
        assertThat(errorString, containsString("""
                ***Error:   Authorisation for [inetnum] 192.168.200.0 - 192.168.200.255 failed
                            using "mnt-irt:"
                            not authenticated by: irt-test
                """));
    }

    @Test
    public void create_new_irt_with_password_using_apikeys_error() {

        final String createIrt = """
                irt:          irt-1test
                address:      RIPE NCC
                e-mail:       irt-dbtest@ripe.net
                auth:         MD5-PW $1$qxm985sj$3OOxndKKw/fgUeQO7baeF/  #irt
                auth:         SSO person@net.net
                irt-nfy:      irt_nfy1_dbtest@ripe.net
                notify:       nfy_dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       OWNER-MNT
                source:       TEST
                """;

        final String errorString = SecureRestTest.target(getSecurePort(), "whois/syncupdates/test?" +
                        "DATA=" + SyncUpdateUtils.encode(createIrt))
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                .get(String.class);

        assertThat(errorString, containsString("Number of objects processed successfully:  0"));
        assertThat(errorString, containsString("""
                ***Error:   MD5 hashed password authentication is deprecated. Please switch to
                            an alternative authentication method.
                """));
    }

    @Test
    public void create_new_irt_without_password_using_apikeys_succeed() {

        final String createIrt = """
                irt:          irt-1test
                address:      RIPE NCC
                e-mail:       irt-dbtest@ripe.net
                auth:         SSO person@net.net
                irt-nfy:      irt_nfy1_dbtest@ripe.net
                notify:       nfy_dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       OWNER-MNT
                source:       TEST
                """;

        final String succeeded = SecureRestTest.target(getSecurePort(), "whois/syncupdates/test?" +
                        "DATA=" + SyncUpdateUtils.encode(createIrt))
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                .get(String.class);

        assertThat(succeeded, containsString("Number of objects processed successfully:  1"));
    }

    @Test
    public void create_new_resource_with_irt_apikey_succeed() {

        final String createResource = """
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       OWNER-MNT
                mnt-irt:      irt-test
                source:       TEST
                """;

        final String errorString = SecureRestTest.target(getSecurePort(), "whois/syncupdates/test?" +
                        "DATA=" + SyncUpdateUtils.encode(createResource))
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                .get(String.class);

        assertThat(errorString, containsString("Number of objects processed successfully:  1"));
    }

    // PUT

    @Test
    public void update_new_mntner_with_password_error() {

        final String updateMntner = """
                mntner:      OWNER-MNT
                descr:       Owner Maintainer
                admin-c:     TP1-TEST
                upd-to:      noreply@ripe.net
                auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test
                auth:        SSO person@net.net
                mnt-by:      OWNER-MNT
                source:      TEST
                """;

        final String errorString = RestTest.target(getPort(),
                        "whois/syncupdates/test?"  + "DATA=" + SyncUpdateUtils.encode(updateMntner + "\npassword: test"))
                        .request()
                        .get(String.class);

        assertThat(errorString, containsString("""
                ***Error:   Authorisation for [mntner] OWNER-MNT failed
                            using "mnt-by:"
                            not authenticated by: OWNER-MNT, OWNER-MNT"""));
        assertThat(errorString, containsString("""
                ***Error:   MD5 hashed password authentication is deprecated. Please switch to
                            an alternative authentication method.
                """));
        assertThat(errorString, containsString("""
            ***Warning: MD5 hashed password authentication has been ignored because is not
                        longer supported."""));
    }

    @Test
    public void update_new_password_mntner_with_apikeys_error() {

        final String updateMntner = """
                mntner:      OWNER-MNT
                descr:       Owner Maintainer
                admin-c:     TP1-TEST
                upd-to:      noreply@ripe.net
                auth:   MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update
                auth:        SSO person@net.net
                mnt-by:      OWNER-MNT
                source:      TEST
                """;

        final String errorString = SecureRestTest.target(getSecurePort(), "whois/syncupdates/test?"  + "DATA=" + SyncUpdateUtils.encode(updateMntner))
                        .request()
                        .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                        .get(String.class);

        assertThat(errorString, containsString("""
                ***Error:   MD5 hashed password authentication is deprecated. Please switch to
                            an alternative authentication method.
                """));
    }

    @Test
    public void update_new_sso_mntner_with_apikeys_succeed() {

        final String updateMntner = """
                mntner:      OWNER-MNT
                descr:       Owner Maintainer
                admin-c:     TP1-TEST
                upd-to:      noreply@ripe.net
                auth:        SSO test@ripe.net
                auth:        SSO person@net.net
                mnt-by:      OWNER-MNT
                source:      TEST
                """;

        final String succeeded = SecureRestTest.target(getSecurePort(),
                        "whois/syncupdates/test?"  + "DATA=" + SyncUpdateUtils.encode(updateMntner))
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                .get(String.class);

        assertThat(succeeded, containsString("Number of objects processed successfully:  1"));
    }

    @Test
    public void update_person_with_password_error() {

        initObjects();
        final String updatePerson = """
                person:    Pauleth Palthen
                address:   Singel 258
                phone:     +31-1234567890
                e-mail:    noreply@ripe.net
                mnt-by:    OWNER-MNT
                nic-hdl:   PP1-TEST
                remarks:   updated remark
                source:    TEST
                """;

        final String errorString = RestTest.target(getPort(),
                        "whois/syncupdates/test?"  + "DATA=" + SyncUpdateUtils.encode(updatePerson + "\npassword: test"))
                        .request()
                        .get(String.class);

        assertThat(errorString, containsString("""
                ***Error:   Authorisation for [person] PP1-TEST failed
                            using "mnt-by:"
                            not authenticated by: OWNER-MNT"""));
        assertThat(errorString, containsString("""
            ***Warning: MD5 hashed password authentication has been ignored because is not
                        longer supported."""));
    }

    @Test
    public void update_person_with_both_crowd_and_password_succeed() {

        initObjects();

        final String updatePerson = """
                person:    Pauleth Palthen
                address:   Singel 258
                phone:     +31-1234567890
                e-mail:    noreply@ripe.net
                mnt-by:    OWNER-MNT
                nic-hdl:   PP1-TEST
                remarks:   updated remark
                source:    TEST
                """;

        final String succeeded = SecureRestTest.target(getSecurePort(),
                        "whois/syncupdates/test?"  + "DATA=" + SyncUpdateUtils.encode(updatePerson+ "\npassword: test"))
                .request()
                .cookie("crowd.token_key", "valid-token")
                .get(String.class);


        assertThat(succeeded, containsString("Number of objects processed successfully:  1"));
    }


    @Test
    public void update_resource_with_irt_password_error() {

        final String updatedResource = """
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       OWNER-MNT
                mnt-irt:      irt-test
                source:       TEST
                """;

        final String errorString = SecureRestTest.target(getSecurePort(), "whois/syncupdates/test?" +
                        "DATA=" + SyncUpdateUtils.encode(updatedResource + "\npassword: irt"))
                .request()
                .get(String.class);

        assertThat(errorString, containsString("Number of objects processed successfully:  0"));
        assertThat(errorString, containsString("""
                ***Warning: MD5 hashed password authentication has been ignored because is not
                            longer supported.
                """));
        assertThat(errorString, containsString("""
                ***Error:   Authorisation for [inetnum] 192.168.0.0 - 192.168.255.255 failed
                            using "mnt-irt:"
                            not authenticated by: irt-test
                """));
    }

    @Test
    public void update_irt_with_password_using_apikeys_error() {

        final String updateIrt = """
                irt:          irt-test
                address:      RIPE NCC 123
                e-mail:       irt-dbtest@ripe.net
                auth:         MD5-PW $1$qxm985sj$3OOxndKKw/fgUeQO7baeF/  #irt
                auth:         SSO person@net.net
                irt-nfy:      irt_nfy1_dbtest@ripe.net
                notify:       nfy_dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       OWNER-MNT
                source:       TEST
                """;

        final String errorString = SecureRestTest.target(getSecurePort(), "whois/syncupdates/test?" +
                        "DATA=" + SyncUpdateUtils.encode(updateIrt))
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                .get(String.class);

        assertThat(errorString, containsString("Number of objects processed successfully:  0"));
        assertThat(errorString, containsString("""
                ***Error:   MD5 hashed password authentication is deprecated. Please switch to
                            an alternative authentication method.
                """));
    }

    @Test
    public void update_irt_without_password_using_apikeys_succeed() {

        final String updateIrt = """
                irt:          irt-test
                address:      RIPE NCC 123
                e-mail:       irt-dbtest@ripe.net
                auth:         SSO person@net.net
                irt-nfy:      irt_nfy1_dbtest@ripe.net
                notify:       nfy_dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       OWNER-MNT
                source:       TEST
                """;

        final String succeeded = SecureRestTest.target(getSecurePort(), "whois/syncupdates/test?" +
                        "DATA=" + SyncUpdateUtils.encode(updateIrt))
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                .get(String.class);

        assertThat(succeeded, containsString("Number of objects processed successfully:  1"));
    }

    @Test
    public void update_resource_with_irt_apikey_succeed() {

        databaseHelper.addObject("""
                organisation:  ORG-OT1-TEST
                org-type:      LIR
                abuse-c:       TP1-TEST
                mnt-by:        OWNER-MNT
                mnt-ref:      OWNER-MNT
                source:        TEST
                """);

        final String updateResource = """
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                org:          ORG-OT1-TEST
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       OWNER-MNT
                mnt-irt:      irt-test
                source:       TEST
                """;

        final String errorString = SecureRestTest.target(getSecurePort(), "whois/syncupdates/test?" +
                        "DATA=" + SyncUpdateUtils.encode(updateResource))
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                .get(String.class);

        assertThat(errorString, containsString("Number of objects processed successfully:  1"));
    }

    // DELETE

    @Test
    public void delete_mntner_with_password_error() {

        final String errorString = RestTest.target(getPort(),
                        "whois/syncupdates/test?"  + "DATA=" + SyncUpdateUtils.encode(OWNER_MNT + "delete: test" + "\npassword: test"))
                        .request()
                        .get(String.class);

        assertThat(errorString, containsString("""
                ***Error:   Authorisation for [mntner] OWNER-MNT failed
                            using "mnt-by:"
                            not authenticated by: OWNER-MNT, OWNER-MNT"""));
        assertThat(errorString, containsString("""
                ***Error:   Object [mntner] OWNER-MNT is referenced from other objects
                """));
        assertThat(errorString, containsString("""
            ***Warning: MD5 hashed password authentication has been ignored because is not
                        longer supported."""));
    }

    @Test
    public void delete_existing_password_mntner_with_apikeys_no_auth_error() {

        final String errorString = SecureRestTest.target(getSecurePort(),
                        "whois/syncupdates/test?"  + "DATA=" + SyncUpdateUtils.encode(OWNER_MNT + "delete: test"))
                        .request()
                        .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                        .get(String.class);

        assertThat(errorString, not(containsString("""
                ***Error:   Authorisation for [mntner] OWNER-MNT failed
                            using "mnt-by:"
                            not authenticated by: OWNER-MNT, OWNER-MNT""")));

        assertThat(errorString, containsString("""
                ***Error:   Object [mntner] OWNER-MNT is referenced from other objects
                """));
    }

    @Test
    public void delete_person_with_password_error() {

        final String personObject = """
                person:    Pauleth Palthen
                address:   Singel 258
                phone:     +31-1234567890
                e-mail:    noreply@ripe.net
                mnt-by:    OWNER-MNT
                nic-hdl:   PP1-TEST
                remarks:   remark
                source:    TEST
                """;

        databaseHelper.addObject(personObject);

        final String errorString = RestTest.target(getPort(),
                        "whois/syncupdates/test?"  + "DATA=" + SyncUpdateUtils.encode(personObject + "delete: test"  + "\npassword: test"))
                        .request()
                        .get(String.class);

        assertThat(errorString, not(containsString("""
                ***Error:   Authorisation for [mntner] OWNER-MNT failed
                            using "mnt-by:"
                            not authenticated by: OWNER-MNT""")));
        assertThat(errorString, containsString("""
            ***Warning: MD5 hashed password authentication has been ignored because is not
                        longer supported."""));
    }

    @Test
    public void delete_person_with_crowd_succeed() {

        final String personObject = """
                person:    Pauleth Palthen
                address:   Singel 258
                phone:     +31-1234567890
                e-mail:    noreply@ripe.net
                mnt-by:    OWNER-MNT
                nic-hdl:   PP1-TEST
                remarks:   remark
                source:    TEST
                """;

        databaseHelper.addObject(personObject);

        final String succeeded = SecureRestTest.target(getSecurePort(),
                        "whois/syncupdates/test?"  + "DATA=" + SyncUpdateUtils.encode(personObject + "delete: test"))
                .request()
                .cookie("crowd.token_key", "valid-token")
                .get(String.class);

        assertThat(succeeded, containsString("Number of objects processed successfully:  1"));
    }


    @Test
    public void delete_person_with_both_crowd_and_password_succeed() {

        final String personObject = """
                person:    Pauleth Palthen
                address:   Singel 258
                phone:     +31-1234567890
                e-mail:    noreply@ripe.net
                mnt-by:    OWNER-MNT
                nic-hdl:   PP1-TEST
                remarks:   remark
                source:    TEST
                """;

        databaseHelper.addObject(personObject);

        final String succeeded = SecureRestTest.target(getSecurePort(),
                        "whois/syncupdates/test?"  + "DATA=" + SyncUpdateUtils.encode(personObject + "delete: test" + "\npassword: test"))
                .request()
                .cookie("crowd.token_key", "valid-token")
                .get(String.class);


        assertThat(succeeded, containsString("Number of objects processed successfully:  1"));
    }

    @Test
    public void delete_irt_with_password_error() {

        final String irtObject = """
                irt:          irt-1test
                address:      RIPE NCC
                e-mail:       irt-dbtest@ripe.net
                auth:         MD5-PW $1$qxm985sj$3OOxndKKw/fgUeQO7baeF/  #irt
                auth:         SSO person@net.net
                irt-nfy:      irt_nfy1_dbtest@ripe.net
                notify:       nfy_dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       OWNER-MNT
                source:       TEST
                """;

        databaseHelper.addObject(irtObject);

        final String errorString = SecureRestTest.target(getSecurePort(),
                        "whois/syncupdates/test?"  + "DATA=" + SyncUpdateUtils.encode(irtObject + "delete: test" + "\npassword: irt"))
                .request()
                .get(String.class);

        assertThat(errorString, not(containsString("""
                ***Error:   Authorisation for [mntner] OWNER-MNT failed
                            using "mnt-by:"
                            not authenticated by: OWNER-MNT""")));
        assertThat(errorString, containsString("""
            ***Warning: MD5 hashed password authentication has been ignored because is not
                        longer supported."""));
    }

    @Test
    public void delete_irt_with_crowd_succeed() {

        final String irtObject = """
                irt:          irt-1test
                address:      RIPE NCC
                e-mail:       irt-dbtest@ripe.net
                auth:         MD5-PW $1$qxm985sj$3OOxndKKw/fgUeQO7baeF/  #irt
                auth:         SSO person@net.net
                irt-nfy:      irt_nfy1_dbtest@ripe.net
                notify:       nfy_dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       OWNER-MNT
                source:       TEST
                """;

        databaseHelper.addObject(irtObject);

        final String succeeded = SecureRestTest.target(getSecurePort(),
                        "whois/syncupdates/test?"  + "DATA=" + SyncUpdateUtils.encode(irtObject + "delete: test"))
                .request()
                .cookie("crowd.token_key", "valid-token")
                .get(String.class);

        assertThat(succeeded, containsString("Number of objects processed successfully:  1"));
    }


    private void initObjects(){
        databaseHelper.addObject("""
                person:    Pauleth Palthen
                address:   Singel 258
                phone:     +31-1234567890
                e-mail:    noreply@ripe.net
                mnt-by:    OWNER-MNT
                nic-hdl:   PP1-TEST
                remarks:   remark
                source:    TEST
                """);
    }

}
