package net.ripe.db.whois.api.rest;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectServerMapper;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.sso.CrowdClient;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@ActiveProfiles(profiles = WhoisProfile.ENDTOEND, inheritProfiles = false)
public class WhoisRestServiceEndToEndTest extends AbstractIntegrationTest {

    public static final String USER1 = "db_e2e_1@ripe.net";
    public static final String PASSWORD1 = "pw_e2e_1";
    public static final String USER2 = "db_e2e_2@ripe.net";
    public static final String PASSWORD2 = "pw_e2e_2";
    public static final String USER3 = "db_e2e_3@ripe.net";     // INACTIVE USER
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

    @Autowired
    WhoisObjectServerMapper whoisObjectMapper;
    @Autowired
    CrowdClient crowdClient;

    @Before
    public void setup() {
        databaseHelper.addObjects(baseFixtures.values());
    }

    private RpslObject makeMntner(String pkey, String... attributes) {
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

    private RpslObject makeInetnum(String pkey, String... attributes) {
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

    private RpslObject buildGenericObject(String object, String... attributes) {
        RpslObjectBuilder builder = new RpslObjectBuilder(object);

        List<RpslAttribute> attributeList = new ArrayList<>();
        for (String attribute : attributes) {
            attributeList.addAll(RpslObjectBuilder.getAttributes(attribute));
        }
        for (RpslAttribute rpslAttribute : attributeList) {
            builder.removeAttributeType(rpslAttribute.getType());
        }

        builder.addAttributes(attributeList);
        return builder.sort().get();
    }

    private void assertUnauthorizedErrormessage(final ClientErrorException e, String... args) {
        assertThat(e.getResponse().getStatus(), is(401));
        final WhoisResources whoisResources = e.getResponse().readEntity(WhoisResources.class);
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

    @Test
    public void Create_assignment_mnt_valid_SSO_only_logged_in() {
        RpslObject LIR_MNT = makeMntner("LIR", "auth: SSO " + USER1);
        RpslObject ALLOC = makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT");
        databaseHelper.addObjects(LIR_MNT, ALLOC);

        RpslObject ASS = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT");

        final String token = crowdClient.login(USER1, PASSWORD1);
        try {
            String whoisResources = RestTest.target(getPort(), "whois/test/inetnum?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .cookie("crowd.token_key", token)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(ASS), MediaType.APPLICATION_XML), String.class);
            System.err.println(whoisResources);
        } catch (ClientErrorException e) {
            reportAndThrowUnknownError(e);
        } finally {
            crowdClient.logout(USER1);
        }
    }

    @Test
    public void Create_assignment_mntby_2valid_SSO_only_logged_in_1st() {
        RpslObject LIR_MNT = makeMntner("LIR", "auth: SSO " + USER1);
        RpslObject LIR2_MNT = makeMntner("LIR2", "auth: SSO " + USER2);
        RpslObject ALLOC = makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT");
        databaseHelper.addObjects(LIR_MNT, LIR2_MNT, ALLOC);

        RpslObject ASS = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT", "mnt-by: LIR2-MNT");

        final String token = crowdClient.login(USER1, PASSWORD1);
        try {
            String whoisResources = RestTest.target(getPort(), "whois/test/inetnum?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .cookie("crowd.token_key", token)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(ASS), MediaType.APPLICATION_XML), String.class);
            System.err.println(whoisResources);
        } catch (ClientErrorException e) {
            reportAndThrowUnknownError(e);
        } finally {
            crowdClient.logout(USER1);
        }
    }

    @Test
    public void Create_assignment_mntby_2valid_SSO_only_logged_in_2nd() {
        RpslObject LIR_MNT = makeMntner("LIR", "auth: SSO " + USER1);
        RpslObject LIR2_MNT = makeMntner("LIR2", "auth: SSO " + USER2);
        RpslObject ALLOC = makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT");
        databaseHelper.addObjects(LIR_MNT, LIR2_MNT, ALLOC);

        RpslObject ASS = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT", "mnt-by: LIR2-MNT");

        final String token = crowdClient.login(USER2, PASSWORD2);
        try {
            String whoisResources = RestTest.target(getPort(), "whois/test/inetnum?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .cookie("crowd.token_key", token)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(ASS), MediaType.APPLICATION_XML), String.class);
            System.err.println(whoisResources);
        } catch (ClientErrorException e) {
            reportAndThrowUnknownError(e);
        } finally {
            crowdClient.logout(USER2);
        }
    }

    @Test
    public void Create_assignment_mntby_2valid_SSO_only_not_logged_in() {
        RpslObject LIR_MNT = makeMntner("LIR", "auth: SSO " + USER1);
        RpslObject LIR2_MNT = makeMntner("LIR2", "auth: SSO " + USER2);
        RpslObject ALLOC = makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT");
        databaseHelper.addObjects(LIR_MNT, LIR2_MNT, ALLOC);

        RpslObject ASS = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT", "mnt-by: LIR2-MNT");

        try {
            RestTest.target(getPort(), "whois/test/inetnum?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(ASS), MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (ClientErrorException e) {
            assertUnauthorizedErrormessage(e, "inetnum", "10.0.0.0 - 10.0.255.255", "mnt-by", "LIR-MNT, LIR2-MNT");
        }
    }

    @Test
    public void Create_assignment_mntby_inactive_SSO_not_logged_in() {
        RpslObject LIR_MNT = makeMntner("LIR", "auth: SSO " + USER3);
        RpslObject ALLOC = makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT");
        databaseHelper.addObjects(LIR_MNT, ALLOC);

        RpslObject ASS = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT");

        try {
            RestTest.target(getPort(), "whois/test/inetnum?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(ASS), MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (ClientErrorException e) {
            assertUnauthorizedErrormessage(e, "inetnum", "10.0.0.0 - 10.0.255.255", "mnt-by", "LIR-MNT");
        }
    }

    @Test
    public void Create_assignment_mntby_2valid_SSO_1pw_logged_in() {
        RpslObject LIR_MNT = makeMntner("LIR", "auth: SSO " + USER1);
        RpslObject LIR2_MNT = makeMntner("LIR2", "auth: SSO " + USER2, "auth: MD5-PW $1$7AEhjSjo$KvxW0YOJFkHpoZqBkpTiO0 # lir");
        RpslObject ALLOC = makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT");
        databaseHelper.addObjects(LIR_MNT, LIR2_MNT, ALLOC);

        RpslObject ASS = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT", "mnt-by: LIR2-MNT");

        final String token = crowdClient.login(USER2, PASSWORD2);
        try {
            final String whoisResources = RestTest.target(getPort(), "whois/test/inetnum?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .cookie("crowd.token_key", token)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(ASS), MediaType.APPLICATION_XML), String.class);
            System.err.println(whoisResources);
        } catch (ClientErrorException e) {
            reportAndThrowUnknownError(e);
        } finally {
            crowdClient.logout(USER2);
        }
    }

    @Test
    public void Create_assignment_mntby_2valid_SSO_1pw_not_logged_in_pw() {
        RpslObject LIR_MNT = makeMntner("LIR", "auth: SSO " + USER1);
        RpslObject LIR2_MNT = makeMntner("LIR2", "auth: SSO " + USER2, "auth: MD5-PW $1$7AEhjSjo$KvxW0YOJFkHpoZqBkpTiO0 # lir");
        RpslObject ALLOC = makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT");
        databaseHelper.addObjects(LIR_MNT, LIR2_MNT, ALLOC);

        RpslObject ASS = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT", "mnt-by: LIR2-MNT");

        try {
            final String whoisResources = RestTest.target(getPort(), "whois/test/inetnum?password=test&password=lir")
                    .request(MediaType.APPLICATION_XML)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(ASS), MediaType.APPLICATION_XML), String.class);
            System.err.println(whoisResources);
        } catch (ClientErrorException e) {
            reportAndThrowUnknownError(e);
        }
    }

    @Test
    public void Create_assignment_mntby_2valid_SSO_1pw_logged_in_pw() {
        RpslObject LIR_MNT = makeMntner("LIR", "auth: SSO " + USER1);
        RpslObject LIR2_MNT = makeMntner("LIR2", "auth: SSO " + USER2, "auth: MD5-PW $1$7AEhjSjo$KvxW0YOJFkHpoZqBkpTiO0 # lir");
        RpslObject ALLOC = makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT");
        databaseHelper.addObjects(LIR_MNT, LIR2_MNT, ALLOC);

        RpslObject ASS = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT", "mnt-by: LIR2-MNT");

        final String token = crowdClient.login(USER1, PASSWORD1);
        try {
            final String whoisResources = RestTest.target(getPort(), "whois/test/inetnum?password=test&password=lir")
                    .request(MediaType.APPLICATION_XML)
                    .cookie("crowd.token_key", token)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(ASS), MediaType.APPLICATION_XML), String.class);
            System.err.println(whoisResources);
        } catch (ClientErrorException e) {
            reportAndThrowUnknownError(e);
        } finally {
            crowdClient.logout(USER1);
        }
    }

    @Test
    public void Create_assignment_mntby_2valid_SSO_1pw_not_logged_in_no_pw() {
        RpslObject LIR_MNT = makeMntner("LIR", "auth: SSO " + USER1);
        RpslObject LIR2_MNT = makeMntner("LIR2", "auth: SSO " + USER2, "auth: MD5-PW $1$7AEhjSjo$KvxW0YOJFkHpoZqBkpTiO0 # lir");
        RpslObject ALLOC = makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT");
        databaseHelper.addObjects(LIR_MNT, LIR2_MNT, ALLOC);

        RpslObject ASS = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT", "mnt-by: LIR2-MNT");

        try {
            RestTest.target(getPort(), "whois/test/inetnum?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(ASS), MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (ClientErrorException e) {
            assertUnauthorizedErrormessage(e, "inetnum", "10.0.0.0 - 10.0.255.255", "mnt-by", "LIR-MNT, LIR2-MNT");
        }
    }

    @Test
    public void Create_assignment_mntby_invalid_SSO_1pw_not_logged_in_pw() {
        RpslObject LIR_MNT = makeMntner("LIR", "auth: SSO " + USER3, "auth: MD5-PW $1$7AEhjSjo$KvxW0YOJFkHpoZqBkpTiO0 # lir");
        RpslObject ALLOC = makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT");
        databaseHelper.addObjects(LIR_MNT, ALLOC);

        RpslObject ASS = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT");

        try {
            final String whoisResources = RestTest.target(getPort(), "whois/test/inetnum?password=test&password=lir")
                    .request(MediaType.APPLICATION_XML)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(ASS), MediaType.APPLICATION_XML), String.class);
            System.err.println(whoisResources);
        } catch (ClientErrorException e) {
            reportAndThrowUnknownError(e);
        }
    }

    @Test
    public void Create_assignment_mntby_no_SSO_1pw_logged_in_no_pw() {
        RpslObject LIR_MNT = makeMntner("LIR", "auth: MD5-PW $1$7AEhjSjo$KvxW0YOJFkHpoZqBkpTiO0 # lir");
        RpslObject ALLOC = makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT");
        databaseHelper.addObjects(LIR_MNT, ALLOC);

        RpslObject ASS = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT");

        final String token = crowdClient.login(USER1, PASSWORD1);
        try {
            RestTest.target(getPort(), "whois/test/inetnum?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .cookie("crowd.token_key", token)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(ASS), MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (ClientErrorException e) {
            assertUnauthorizedErrormessage(e, "inetnum", "10.0.0.0 - 10.0.255.255", "mnt-by", "LIR-MNT");
        } finally {
            crowdClient.logout(USER1);
        }
    }

    @Test
    public void Create_assignment_mntby1_pw_mntby2_SSO_mntby3_SSO_logged_in_no_pw() {
        RpslObject LIR_MNT = makeMntner("LIR", "auth: MD5-PW $1$7AEhjSjo$KvxW0YOJFkHpoZqBkpTiO0 # lir");
        RpslObject LIR2_MNT = makeMntner("LIR2", "auth: SSO " + USER1);
        RpslObject LIR3_MNT = makeMntner("LIR3", "auth: SSO " + USER2);
        RpslObject ALLOC = makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT");
        databaseHelper.addObjects(LIR_MNT, LIR2_MNT, LIR3_MNT, ALLOC);

        RpslObject ASS = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT", "mnt-by: LIR2-MNT", "mnt-by: LIR3-MNT");

        final String token = crowdClient.login(USER2, PASSWORD2);
        try {
            final String whoisResources = RestTest.target(getPort(), "whois/test/inetnum?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .cookie("crowd.token_key", token)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(ASS), MediaType.APPLICATION_XML), String.class);
            System.err.println(whoisResources);
        } catch (ClientErrorException e) {
            reportAndThrowUnknownError(e);
        } finally {
            crowdClient.logout(USER2);
        }
    }

    @Test
    public void Create_assignment_mntby1_pw_mntby2_SSO_mntby3_SSO_not_logged_in_pw() {
        RpslObject LIR_MNT = makeMntner("LIR", "auth: MD5-PW $1$7AEhjSjo$KvxW0YOJFkHpoZqBkpTiO0 # lir");
        RpslObject LIR2_MNT = makeMntner("LIR2", "auth: SSO " + USER1);
        RpslObject LIR3_MNT = makeMntner("LIR3", "auth: SSO " + USER2);
        RpslObject ALLOC = makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT");
        databaseHelper.addObjects(LIR_MNT, LIR2_MNT, LIR3_MNT, ALLOC);

        RpslObject ASS = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT", "mnt-by: LIR2-MNT", "mnt-by: LIR3-MNT");

        try {
            final String whoisResources = RestTest.target(getPort(), "whois/test/inetnum?password=test&password=lir")
                    .request(MediaType.APPLICATION_XML)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(ASS), MediaType.APPLICATION_XML), String.class);
            System.err.println(whoisResources);
        } catch (ClientErrorException e) {
            reportAndThrowUnknownError(e);
        }
    }

    @Test
    public void Create_assignment_mntby_pw_mnt2_SSO_mnt3_SSO_logged_in_no_pw() {
        // FIXME: unclear
    }

    @Test
    public void Create_assignment_mntby_pw_mnt2_SSO_mnt3_SSO_not_logged_in_pw() {
        // FIXME: unclear
    }

    @Test
    public void Create_assignment_mntby1_SSO1_mntby2_SSO2_mntby3_pw_logged_in_SSO2_no_pw() {
        // FIXME: duplicate
    }

    @Test
    public void Modify_assignment_mntby_valid_SSO_1pw_logged_in() {
        RpslObject LIR_MNT = makeMntner("LIR", "auth: SSO " + USER1);
        RpslObject ALLOC = makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT");
        RpslObject ASS = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT");
        databaseHelper.addObjects(LIR_MNT, ALLOC, ASS);

        RpslObject updatedAss = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT", "remarks: updated");

        final String token = crowdClient.login(USER1, PASSWORD1);
        try {
            String whoisResources = RestTest.target(getPort(), "whois/test/inetnum/10.0.0.0 - 10.0.255.255")
                    .request(MediaType.APPLICATION_XML)
                    .cookie("crowd.token_key", token)
                    .put(Entity.entity(whoisObjectMapper.mapRpslObjects(updatedAss), MediaType.APPLICATION_XML), String.class);
            System.err.println(whoisResources);
        } catch (ClientErrorException e) {
            reportAndThrowUnknownError(e);
        } finally {
            crowdClient.logout(USER1);
        }
    }

    @Test
    public void Delete_assignment_mntby_valid_SSO_1pw_logged_in() {
        RpslObject LIR_MNT = makeMntner("LIR", "auth: SSO " + USER1);
        RpslObject ALLOC = makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT");
        RpslObject ASS = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT");
        databaseHelper.addObjects(LIR_MNT, ALLOC, ASS);

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
    }

    @Test
    public void Create_assignment_mntby_invalid_SSO_token_is_ignored() {
        RpslObject LIR_MNT = makeMntner("LIR", "auth: SSO " + USER1);
        RpslObject ALLOC = makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT");
        databaseHelper.addObjects(LIR_MNT, ALLOC);

        RpslObject ASS = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT");

        final String token = "deadbeef";
        try {
            WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/inetnum")
                    .request(MediaType.APPLICATION_XML)
                    .cookie("crowd.token_key", token)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(ASS), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (ClientErrorException e) {
            assertThat(e.getResponse().getStatus(), is(401));
            final WhoisResources whoisResources = e.getResponse().readEntity(WhoisResources.class);
            final ErrorMessage errorMessage = Lists.reverse(whoisResources.getErrorMessages()).get(0);
            assertThat(errorMessage.getText(), is("SSO token ignored (%s)"));
            assertThat(errorMessage.getArgs().size(), is(1));
            assertThat(errorMessage.getArgs().get(0).getValue(), is("Unknown RIPE Access token: deadbeef"));
        }
    }
}
