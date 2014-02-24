package net.ripe.db.whois.api.rest;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectServerMapper;
import net.ripe.db.whois.common.collect.IterableTransformer;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.sso.CrowdClient;
import net.ripe.db.whois.common.support.FileHelper;
import net.ripe.db.whois.update.support.TestUpdateLog;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ActiveProfiles;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.text.MessageFormat;
import java.util.Deque;
import java.util.List;

import static net.ripe.db.whois.common.rpsl.RpslObjectFilter.buildGenericObject;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

// TODO: [ES] dependency on testlab properties in main whois.properties (should be separated into environment specific file)
@ActiveProfiles(profiles = WhoisProfile.ENDTOEND, inheritProfiles = false)
public class WhoisRestServiceEndToEndTest extends AbstractIntegrationTest {

    // accounts used for testing on serval.testlab
    public static final String USER1 = "db_e2e_1@ripe.net";
    public static final String PASSWORD1 = "pw_e2e_1";
    public static final String USER2 = "db_e2e_2@ripe.net";
    public static final String PASSWORD2 = "pw_e2e_2";
    public static final String INACTIVE_USER = "db_e2e_3@ripe.net";
    public static final String PASSWORD3 = "pw_e2e_3";

    private static ImmutableMap<String, RpslObject> baseFixtures = ImmutableMap.<String, RpslObject>builder()
            .put("OWNER-MNT", RpslObject.parse("" +
                    "mntner:      OWNER-MNT\n" +
                    "descr:       Owner Maintainer\n" +
                    "admin-c:     TP1-TEST\n" +
                    "upd-to:      noreply@ripe.net\n" +
                    "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                    "mnt-by:      OWNER-MNT\n" +
                    "referral-by: OWNER-MNT\n" +
                    "changed:     dbtest@ripe.net 20120101\n" +
                    "source:      TEST"))

            .put("RIPE-NCC-HM-MNT", RpslObject.parse("" +
                    "mntner:      RIPE-NCC-HM-MNT\n" +
                    "descr:       hostmaster MNTNER\n" +
                    "admin-c:     TP1-TEST\n" +
                    "upd-to:      updto_hm@ripe.net\n" +
                    "mnt-nfy:     mntnfy_hm@ripe.net\n" +
                    "notify:      notify_hm@ripe.net\n" +
                    "auth:        MD5-PW $1$mV2gSZtj$1oVwjZr0ecFZQHsNbw2Ss.  #hm\n" +
                    "mnt-by:      RIPE-NCC-HM-MNT\n" +
                    "referral-by: RIPE-NCC-HM-MNT\n" +
                    "changed:     dbtest@ripe.net\n" +
                    "source:      TEST"))

            .put("END-USER-MNT", RpslObject.parse("" +
                    "mntner:      END-USER-MNT\n" +
                    "descr:       used for lir\n" +
                    "admin-c:     TP1-TEST\n" +
                    "upd-to:      updto_lir@ripe.net\n" +
                    "auth:        MD5-PW $1$4qnKkEY3$9NduUoRMNiBbAX9QEDMkh1  #end\n" +
                    "mnt-by:      END-USER-MNT\n" +
                    "referral-by: END-USER-MNT\n" +
                    "changed:     dbtest@ripe.net 20120101\n" +
                    "source:      TEST"))

            .put("TP1-TEST", RpslObject.parse("" +
                    "person:    Test Person\n" +
                    "address:   Singel 258\n" +
                    "phone:     +31 6 12345678\n" +
                    "nic-hdl:   TP1-TEST\n" +
                    "mnt-by:    OWNER-MNT\n" +
                    "changed:   dbtest@ripe.net 20120101\n" +
                    "source:    TEST\n"))

            .put("TR1-TEST", RpslObject.parse("" +
                    "role:      Test Role\n" +
                    "address:   Singel 258\n" +
                    "phone:     +31 6 12345678\n" +
                    "nic-hdl:   TR1-TEST\n" +
                    "admin-c:   TR1-TEST\n" +
                    "abuse-mailbox: abuse@test.net\n" +
                    "mnt-by:    OWNER-MNT\n" +
                    "changed:   dbtest@ripe.net 20120101\n" +
                    "source:    TEST\n"))

            .put("ORG-LIR1-TEST", RpslObject.parse("" +
                    "organisation:    ORG-LIR1-TEST\n" +
                    "org-type:        LIR\n" +
                    "org-name:        Local Internet Registry\n" +
                    "address:         RIPE NCC\n" +
                    "e-mail:          dbtest@ripe.net\n" +
                    "ref-nfy:         dbtest-org@ripe.net\n" +
                    "mnt-ref:         OWNER-MNT\n" +
                    "mnt-by:          OWNER-MNT\n" +
                    "changed: denis@ripe.net 20121016\n" +
                    "source:  TEST\n"))

            .build();

    @Autowired WhoisObjectServerMapper whoisObjectMapper;
    @Autowired CrowdClient crowdClient;

    @Autowired TestUpdateLog updateLog;
    @Value("${dir.update.audit.log}") String auditLog;

    @Before
    public void setup() {
        databaseHelper.addObjects(baseFixtures.values());
        testDateTimeProvider.setTime(LocalDateTime.parse("2001-02-06T17:00:00"));
    }

    @Test
    public void create_assignment_mnt_valid_SSO_only_logged_in() throws Exception {
        databaseHelper.addObjects(
                makeMntner("LIR", "auth: SSO " + USER1),
                makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT"));

        final RpslObject assignment = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT");
        final String token = crowdClient.login(USER1, PASSWORD1);

        try {
            final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/inetnum?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .cookie("crowd.token_key", token)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(assignment), MediaType.APPLICATION_XML), WhoisResources.class);

            assertThat(whoisResources.getErrorMessages(), emptyIterable());
            assertThat(whoisResources.getWhoisObjects(), hasSize(1));
            assertThat(whoisResources.getWhoisObjects().get(0).getPrimaryKey().get(0).getValue(), is("10.0.0.0 - 10.0.255.255"));
        } catch (ClientErrorException e) {
            reportAndThrowUnknownError(e);
        } finally {
            crowdClient.logout(USER1);
        }
    }

    @Test
    public void create_assignment_mntby_2valid_SSO_only_logged_in_1st() throws Exception {
        databaseHelper.addObjects(
                makeMntner("LIR", "auth: SSO " + USER1, "auth: SSO " + USER2),
                makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT"));

        final RpslObject assignment = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT");
        final String token = crowdClient.login(USER1, PASSWORD1);

        try {
            final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/inetnum?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .cookie("crowd.token_key", token)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(assignment), MediaType.APPLICATION_XML), WhoisResources.class);

            assertThat(whoisResources.getErrorMessages(), emptyIterable());
            assertThat(whoisResources.getWhoisObjects(), hasSize(1));
            assertThat(whoisResources.getWhoisObjects().get(0).getPrimaryKey().get(0).getValue(), is("10.0.0.0 - 10.0.255.255"));
        } catch (ClientErrorException e) {
            reportAndThrowUnknownError(e);
        } finally {
            crowdClient.logout(USER1);
        }
    }

    @Test
    public void create_assignment_mntby_2valid_SSO_only_logged_in_2nd() throws Exception {
        databaseHelper.addObjects(
                makeMntner("LIR", "auth: SSO " + USER1, "auth: SSO " + USER2),
                makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT"));

        final RpslObject assignment = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT");
        final String token = crowdClient.login(USER2, PASSWORD2);

        try {
            final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/inetnum?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .cookie("crowd.token_key", token)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(assignment), MediaType.APPLICATION_XML), WhoisResources.class);

            assertThat(whoisResources.getErrorMessages(), emptyIterable());
            assertThat(whoisResources.getWhoisObjects(), hasSize(1));
            assertThat(whoisResources.getWhoisObjects().get(0).getPrimaryKey().get(0).getValue(), is("10.0.0.0 - 10.0.255.255"));
        } catch (ClientErrorException e) {
            reportAndThrowUnknownError(e);
        } finally {
            crowdClient.logout(USER2);
        }
    }

    @Test
    public void create_assignment_mntby_2valid_SSO_only_not_logged_in() {
        databaseHelper.addObjects(
                makeMntner("LIR", "auth: SSO " + USER1, "auth: SSO " + USER2),
                makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT"));

        final RpslObject assignment = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT");

        try {
            RestTest.target(getPort(), "whois/test/inetnum?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(assignment), MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (NotAuthorizedException expected) {
            assertUnauthorizedErrorMessage(expected, "inetnum", "10.0.0.0 - 10.0.255.255", "mnt-by", "LIR-MNT");
        }
    }

    @Test
    public void create_assignment_mntby_inactive_SSO_not_logged_in() {
        databaseHelper.addObjects(
                makeMntner("LIR", "auth: SSO " + INACTIVE_USER),
                makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT"));

        final RpslObject assignment = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT");

        try {
            RestTest.target(getPort(), "whois/test/inetnum?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(assignment), MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (NotAuthorizedException expected) {
            assertUnauthorizedErrorMessage(expected, "inetnum", "10.0.0.0 - 10.0.255.255", "mnt-by", "LIR-MNT");
        }
    }

    @Test
    public void create_object_with_SSO_and_passwd_maintainer_auth_by_SSO_and_passwd() throws Exception {
        databaseHelper.addObjects(
                makeMntner("LIR", "auth: SSO " + USER1, "auth: SSO " + USER2, "auth: MD5-PW $1$7AEhjSjo$KvxW0YOJFkHpoZqBkpTiO0 # lir"),
                makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT"));

        final RpslObject assignment = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT");

        final String token = crowdClient.login(USER2, PASSWORD2);
        try {
            final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/inetnum?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .cookie("crowd.token_key", token)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(assignment), MediaType.APPLICATION_XML), WhoisResources.class);

            assertThat(whoisResources.getErrorMessages(), emptyIterable());
            assertThat(whoisResources.getWhoisObjects(), hasSize(1));
            assertThat(whoisResources.getWhoisObjects().get(0).getPrimaryKey().get(0).getValue(), is("10.0.0.0 - 10.0.255.255"));
        } catch (ClientErrorException e) {
            reportAndThrowUnknownError(e);
        } finally {
            crowdClient.logout(USER2);
        }
    }

    @Test
    public void create_object_with_SSO_and_passwd_maintainer_auth_by_passwd_only() {
        databaseHelper.addObjects(
                makeMntner("LIR", "auth: SSO " + USER1, "auth: SSO " + USER2, "auth: MD5-PW $1$7AEhjSjo$KvxW0YOJFkHpoZqBkpTiO0 # lir"),
                makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT"));

        final RpslObject assignment = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT");

        try {
            final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/inetnum?password=test&password=lir")
                    .request(MediaType.APPLICATION_XML)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(assignment), MediaType.APPLICATION_XML), WhoisResources.class);

            assertThat(whoisResources.getErrorMessages(), emptyIterable());
            assertThat(whoisResources.getWhoisObjects(), hasSize(1));
            assertThat(whoisResources.getWhoisObjects().get(0).getPrimaryKey().get(0).getValue(), is("10.0.0.0 - 10.0.255.255"));
        } catch (ClientErrorException e) {
            reportAndThrowUnknownError(e);
        }
    }

    @Test
    public void create_auth_with_logged_in_SSO_and_correct_passwds() throws Exception {
        databaseHelper.addObjects(
                makeMntner("LIR", "auth: SSO " + USER1, "auth: SSO " + USER2, "auth: MD5-PW $1$7AEhjSjo$KvxW0YOJFkHpoZqBkpTiO0 # lir"),
                makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT"));

        final RpslObject assignment = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT");

        final String token = crowdClient.login(USER2, PASSWORD2);
        try {
            final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/inetnum?password=test&password=lir")
                    .request(MediaType.APPLICATION_XML)
                    .cookie("crowd.token_key", token)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(assignment), MediaType.APPLICATION_XML), WhoisResources.class);

            assertThat(whoisResources.getErrorMessages(), emptyIterable());
            assertThat(whoisResources.getWhoisObjects(), hasSize(1));
            assertThat(whoisResources.getWhoisObjects().get(0).getPrimaryKey().get(0).getValue(), is("10.0.0.0 - 10.0.255.255"));
        } catch (ClientErrorException e) {
            reportAndThrowUnknownError(e);
        } finally {
            crowdClient.logout(USER2);
        }
    }

    @Test
    public void create_not_logged_in_SSO_does_not_auth_with_incorrect_passwd() throws Exception {
        databaseHelper.addObjects(
                makeMntner("LIR", "auth: SSO " + USER1, "auth: SSO " + USER2, "auth: MD5-PW $1$7AEhjSjo$KvxW0YOJFkHpoZqBkpTiO0 # lir"),
                makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT"));

        final RpslObject assignment = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT");

        try {
            RestTest.target(getPort(), "whois/test/inetnum?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(assignment), MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (NotAuthorizedException expected) {
            assertUnauthorizedErrorMessage(expected, "inetnum", "10.0.0.0 - 10.0.255.255", "mnt-by", "LIR-MNT");
        }
    }

    @Test
    public void create_logged_in_SSO_does_not_auth_when_object_has_no_SSO_maintainer() throws Exception {
        databaseHelper.addObjects(
                makeMntner("LIR", "auth: MD5-PW $1$7AEhjSjo$KvxW0YOJFkHpoZqBkpTiO0 # lir"),
                makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT"));

        final RpslObject assignment = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT");
        final String token = crowdClient.login(USER1, PASSWORD1);

        try {
            RestTest.target(getPort(), "whois/test/inetnum?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .cookie("crowd.token_key", token)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(assignment), MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (NotAuthorizedException expected) {
            assertUnauthorizedErrorMessage(expected, "inetnum", "10.0.0.0 - 10.0.255.255", "mnt-by", "LIR-MNT");
        } finally {
            crowdClient.logout(USER1);
        }
    }

    @Test
    public void create_object_with_SSO_and_passwd_maintainers_auth_by_passwd_only() throws Exception {
        databaseHelper.addObjects(
                makeMntner("LIR", "auth: MD5-PW $1$7AEhjSjo$KvxW0YOJFkHpoZqBkpTiO0 # lir"),
                makeMntner("LIR2", "auth: SSO " + USER1),
                makeMntner("LIR3", "auth: SSO " + USER2),
                makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT"));

        final RpslObject assignment = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT", "mnt-by: LIR2-MNT", "mnt-by: LIR3-MNT");

        try {
            final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/inetnum?password=test&password=lir")
                    .request(MediaType.APPLICATION_XML)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(assignment), MediaType.APPLICATION_XML), WhoisResources.class);

            assertThat(whoisResources.getErrorMessages(), emptyIterable());
            assertThat(whoisResources.getWhoisObjects(), hasSize(1));
            assertThat(whoisResources.getWhoisObjects().get(0).getPrimaryKey().get(0).getValue(), is("10.0.0.0 - 10.0.255.255"));
        } catch (ClientErrorException e) {
            reportAndThrowUnknownError(e);
        }
    }

    @Test
    public void create_object_with_SSO_and_passwd_maintainers_not_auth_by_SSO() throws Exception {
        databaseHelper.addObjects(
                makeMntner("LIR", "auth: MD5-PW $1$7AEhjSjo$KvxW0YOJFkHpoZqBkpTiO0 # lir"),
                makeMntner("LIR2", "auth: SSO " + USER1),
                makeMntner("LIR3", "auth: SSO " + INACTIVE_USER),
                makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT"));

        final RpslObject assignment = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT", "mnt-by: LIR2-MNT", "mnt-by: LIR3-MNT");

        try {
            RestTest.target(getPort(), "whois/test/inetnum?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(assignment), MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (NotAuthorizedException expected) {
            assertUnauthorizedErrorMessage(expected, "inetnum", "10.0.0.0 - 10.0.255.255", "mnt-by", "LIR-MNT, LIR2-MNT, LIR3-MNT");
        }
    }

    @Test
    public void create_object_with_SSO_and_passwd_maintainers_auth_by_SSO_and_passwd() throws Exception {
        databaseHelper.addObjects(
                makeMntner("LIR", "auth: SSO " + USER1),
                makeMntner("LIR2", "auth: SSO " + USER2),
                makeMntner("LIR3", "auth: MD5-PW $1$7AEhjSjo$KvxW0YOJFkHpoZqBkpTiO0 # lir"),
                makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT"));

        final RpslObject assignment = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT", "mnt-by: LIR2-MNT", "mnt-by: LIR3-MNT");
        final String token = crowdClient.login(USER2, PASSWORD2);

        try {
            final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/inetnum?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .cookie("crowd.token_key", token)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(assignment), MediaType.APPLICATION_XML), WhoisResources.class);

            assertThat(whoisResources.getErrorMessages(), emptyIterable());
            assertThat(whoisResources.getWhoisObjects(), hasSize(1));
            assertThat(whoisResources.getWhoisObjects().get(0).getPrimaryKey().get(0).getValue(), is("10.0.0.0 - 10.0.255.255"));
        } catch (ClientErrorException e) {
            reportAndThrowUnknownError(e);
        } finally {
            crowdClient.logout(USER2);
        }
    }

    @Test
    public void modify_using_SSO_auth_only() throws Exception {
        databaseHelper.addObjects(
                makeMntner("LIR", "auth: SSO " + USER1, "auth: MD5-PW $1$7AEhjSjo$KvxW0YOJFkHpoZqBkpTiO0 # lir"),
                makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT"),
                makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT"));

        final RpslObject updatedAssignment = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT", "remarks: updated");
        final String token = crowdClient.login(USER1, PASSWORD1);

        try {
            final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/inetnum/10.0.0.0 - 10.0.255.255")
                    .request(MediaType.APPLICATION_XML)
                    .cookie("crowd.token_key", token)
                    .put(Entity.entity(whoisObjectMapper.mapRpslObjects(updatedAssignment), MediaType.APPLICATION_XML), WhoisResources.class);

            assertThat(whoisResources.getErrorMessages(), emptyIterable());
            assertThat(whoisResources.getWhoisObjects(), hasSize(1));
            assertThat(whoisResources.getWhoisObjects().get(0).getPrimaryKey().get(0).getValue(), is("10.0.0.0 - 10.0.255.255"));
        } catch (ClientErrorException e) {
            reportAndThrowUnknownError(e);
        } finally {
            crowdClient.logout(USER1);
        }
    }

    @Test
    public void delete_using_SSO_auth_only() throws Exception {
        databaseHelper.addObjects(
                makeMntner("LIR", "auth: SSO " + USER1, "auth: MD5-PW $1$7AEhjSjo$KvxW0YOJFkHpoZqBkpTiO0 # lir"),
                makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT"),
                makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT"));

        final RpslObject toBeDeleted = databaseHelper.lookupObject(ObjectType.INETNUM, "10.0.0.0 - 10.0.255.255");
        assertThat(toBeDeleted, is(not(nullValue())));

        final String token = crowdClient.login(USER1, PASSWORD1);

        try {
            RestTest.target(getPort(), "whois/test/inetnum/10.0.0.0 - 10.0.255.255")
                    .request(MediaType.APPLICATION_XML)
                    .cookie("crowd.token_key", token)
                    .delete();
        } catch (ClientErrorException e) {
            reportAndThrowUnknownError(e);
        } finally {
            crowdClient.logout(USER1);
        }

        try {
            databaseHelper.lookupObject(ObjectType.INETNUM, "10.0.0.0 - 10.0.255.255");
            fail();
        } catch (EmptyResultDataAccessException expected) {
        }
    }

    @Test
    public void invalid_SSO_token_does_not_authenticate() throws Exception {
        databaseHelper.addObjects(
                makeMntner("LIR", "auth: SSO " + USER1),
                makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT"));

        final RpslObject assignment = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT");
        final String token = "deadbeef";

        try {
            RestTest.target(getPort(), "whois/test/inetnum")
                    .request(MediaType.APPLICATION_XML)
                    .cookie("crowd.token_key", token)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(assignment), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (NotAuthorizedException expected) {
            final WhoisResources whoisResources = expected.getResponse().readEntity(WhoisResources.class);
            final ErrorMessage errorMessage = Lists.reverse(whoisResources.getErrorMessages()).get(0);
            assertThat(errorMessage.getText(), is("RIPE NCC Access token ignored"));
        }
    }

    @Test
    public void sso_authentication_takes_precedence_over_password() throws Exception {
        RpslObject person = buildGenericObject(baseFixtures.get("TP1-TEST"), "nic-hdl: TP2-TEST", "mnt-by: LIR-MNT");
        databaseHelper.addObjects(person,
                makeMntner("LIR", "auth: SSO " + USER1, "auth: MD5-PW $1$7AEhjSjo$KvxW0YOJFkHpoZqBkpTiO0 # lir"));

        RpslObject updatedPerson = buildGenericObject(person, "remarks: look at me, all updated");

        final String token = crowdClient.login(USER1, PASSWORD1);

        try {
            String whoisResources = RestTest.target(getPort(), "whois/test/person/TP2-TEST?password=lir")
                    .request(MediaType.APPLICATION_XML)
                    .cookie("crowd.token_key", token)
                    .header(HttpHeaders.X_FORWARDED_FOR, "10.20.30.40")
                    .put(Entity.entity(whoisObjectMapper.mapRpslObjects(updatedPerson), MediaType.APPLICATION_XML), String.class);
            System.err.println(whoisResources);
        } catch (ClientErrorException e) {
            reportAndThrowUnknownError(e);
        } finally {
            crowdClient.logout(USER1);
        }

        final String audit = FileHelper.fetchGzip(new File(auditLog + "/20010206/170000.rest_10.20.30.40_0/000.audit.xml.gz"));
        final Iterable<String> linesContainingPassword = new IterableTransformer<String>(Splitter.on('\n').split(audit)) {
            @Override
            public void apply(String input, Deque<String> result) {
                if (input.toLowerCase().contains("password")) {
                    result.add(input.trim());
                }
            }
        };

        assertThat(linesContainingPassword, contains("<message><![CDATA[/whois/test/person/TP2-TEST?password=lir]]></message>",
                "<credential>PasswordCredential{password = 'lir'}</credential>"));
    }

    // helper methods

    private RpslObject makeMntner(final String pkey, final String... attributes) {
        return buildGenericObject(MessageFormat.format("" +
                "mntner:      {0}-MNT\n" +
                "descr:       used for lir\n" +
                "admin-c:     TP1-TEST\n" +
                "upd-to:      updto_{0}@ripe.net\n" +
                "mnt-nfy:     mntnfy_{0}@ripe.net\n" +
                "notify:      notify_{0}@ripe.net\n" +
                "mnt-by:      {0}-MNT\n" +
                "referral-by: {0}-MNT\n" +
                "changed:     dbtest@ripe.net\n" +
                "source:      TEST", pkey), attributes);
    }

    private RpslObject makeInetnum(final String pkey, final String... attributes) {
        return buildGenericObject(MessageFormat.format("" +
                "inetnum:      {0}\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "org:          ORG-LIR1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "status:       ALLOCATED PA\n" +
                "changed:      dbtest@ripe.net 20020101\n" +
                "source:    TEST\n", pkey), attributes);
    }

    private void assertUnauthorizedErrorMessage(final NotAuthorizedException exception, final String... args) {
        final WhoisResources whoisResources = exception.getResponse().readEntity(WhoisResources.class);
        final List<ErrorMessage> errorMessages = whoisResources.getErrorMessages();
        assertThat(errorMessages.size(), is(1));
        assertThat(errorMessages.get(0).getText(), endsWith("\nnot authenticated by: %s"));
        assertThat(errorMessages.get(0).getArgs().size(), is(args.length));
        for (int i = 0; i < args.length; i++) {
            assertThat(errorMessages.get(0).getArgs().get(i).getValue(), is(args[i]));
        }
    }

    private void reportAndThrowUnknownError(final ClientErrorException e) {
        System.err.println(e.getResponse().getStatus());
        System.err.println(e.getResponse().readEntity(String.class));
        throw e;
    }
}
