package net.ripe.db.whois.api.rest;


import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.MaintenanceMode;
import net.ripe.db.whois.common.TestDateTimeProvider;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import org.glassfish.jersey.uri.UriComponent;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class UpdateMaintainerSS0TestIntegration extends AbstractIntegrationTest {

    private static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:      OWNER-MNT\n" +
            "descr:       Owner Maintainer\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "auth:        SSO person@net.net\n" +
            "mnt-by:      OWNER-MNT\n" +
            "source:      TEST");

    private static final RpslObject TEST_PERSON = RpslObject.parse("" +
            "person:    Test Person\n" +
            "address:   Singel 258\n" +
            "phone:     +31 6 12345678\n" +
            "nic-hdl:   TP1-TEST\n" +
            "mnt-by:    OWNER-MNT\n" +
            "source:    TEST\n");

    private static final RpslObject TEST_ROLE = RpslObject.parse("" +
            "role:      Test Role\n" +
            "address:   Singel 258\n" +
            "phone:     +31 6 12345678\n" +
            "nic-hdl:   TR1-TEST\n" +
            "admin-c:   TR1-TEST\n" +
            "abuse-mailbox: abuse@test.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "source:    TEST\n");

    @Autowired private WhoisObjectMapper whoisObjectMapper;
    @Autowired private MaintenanceMode maintenanceMode;
    @Autowired private TestDateTimeProvider testDateTimeProvider;
    @Autowired private IpRanges ipRanges;

    @Before
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject("role: Test Role\nnic-hdl: TR1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TEST_PERSON);
        databaseHelper.updateObject(TEST_ROLE);
        maintenanceMode.set("FULL,FULL");
        testDateTimeProvider.setTime(LocalDateTime.parse("2001-02-04T17:00:00"));
        //add localhost as not trusted
        ipRanges.setTrusted("::1");
    }

    @Test
    public void update_maintainer_sso_with_sync_false_succeeds() {
        insertSyncHistory("ORG-TEST","OWNER-MNT", 1000, false);
        insertSyncHistory("ORG-TEST","OWNER-MNT", 2000, true);
        insertSyncHistory("ORG-TEST","OWNER-MNT", 3000, false);

        final RpslObject updatedObject = new RpslObjectBuilder(OWNER_MNT).append(new RpslAttribute(AttributeType.AUTH, "SSO test@ripe.net")).get();

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT")
                .request(MediaType.APPLICATION_XML)
                .cookie("crowd.token_key", "valid-token")
                .put(Entity.entity(map(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), hasItem(new Attribute("auth", "SSO person@net.net")));
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), hasItem(new Attribute("auth", "SSO test@ripe.net")));

        RpslObject rpslObject = databaseHelper.lookupObject(ObjectType.MNTNER, "OWNER-MNT");
        assertThat(rpslObject.findAttributes(AttributeType.AUTH),
                containsInAnyOrder(
                        new RpslAttribute(AttributeType.AUTH, "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test"),
                        new RpslAttribute(AttributeType.AUTH, "SSO 906635c2-0405-429a-800b-0602bd716124"),
                        new RpslAttribute(AttributeType.AUTH, "SSO 8ffe29be-89ef-41c8-ba7f-0e1553a623e5"))
        );
    }

    @Test
    public void update_maintainer_add_sso_with_sync_enabled_should_fail() {
        insertSyncHistory("ORG-TEST","OWNER-MNT", 1000, false);
        insertSyncHistory("ORG-TEST","OWNER-MNT", 2000, true);

        final RpslObject updatedObject = new RpslObjectBuilder(OWNER_MNT).append(new RpslAttribute(AttributeType.AUTH, "SSO test@ripe.net")).get();

        try {
            RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT")
                    .request(MediaType.APPLICATION_XML)
                    .cookie("crowd.token_key", "valid-token")
                    .put(Entity.entity(map(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            RestTest.assertOnlyErrorMessage(e, "Error", "You cannot update SSO auth attribute(s), because the maintainer is synchronised from the LIR Portal");
        }
    }

    @Test
    public void update_maintainer_remove_sso_with_sync_enabled_should_fail() {
        insertSyncHistory("ORG-TEST","OWNER-MNT", 1000, false);
        insertSyncHistory("ORG-TEST","OWNER-MNT", 2000, true);

        final RpslObject updatedObject = new RpslObjectBuilder(OWNER_MNT).removeAttribute(new RpslAttribute(AttributeType.AUTH, "SSO person@net.net")).get();

        try {
            RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT")
                    .request(MediaType.APPLICATION_XML)
                    .cookie("crowd.token_key", "valid-token")
                    .put(Entity.entity(map(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            RestTest.assertOnlyErrorMessage(e, "Error", "You cannot update SSO auth attribute(s), because the maintainer is synchronised from the LIR Portal");
        }
    }

    @Test
    public void update_maintainer_update_sso_with_sync_enabled_should_fail() {
        insertSyncHistory("ORG-TEST","OWNER-MNT", 1000, false);
        insertSyncHistory("ORG-TEST","OWNER-MNT", 2000, true);

        final RpslObject updatedObject = new RpslObjectBuilder(OWNER_MNT).removeAttribute(new RpslAttribute(AttributeType.AUTH, "SSO person@net.net")).get();

        try {
            RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT")
                    .request(MediaType.APPLICATION_XML)
                    .cookie("crowd.token_key", "valid-token")
                    .put(Entity.entity(map(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            RestTest.assertOnlyErrorMessage(e, "Error", "You cannot update SSO auth attribute(s), because the maintainer is synchronised from the LIR Portal");
        }
    }

    @Test
    public void update_maintainer_succeeds_sync_enabled_no_sso() {
        insertSyncHistory("ORG-TEST","OWNER-MNT-SYNC", 1000, true);

        RpslObject MNT_SYNC = RpslObject.parse("" +
                "mntner:      OWNER-MNT-SYNC\n" +
                "descr:       Owner Maintainer\n" +
                "admin-c:     TP1-TEST\n" +
                "upd-to:      noreply@ripe.net\n" +
                "auth:        SSO person@net.net\n" +
                "mnt-by:      OWNER-MNT-SYNC\n" +
                "source:      TEST");
        databaseHelper.addObject(MNT_SYNC);

        final RpslObject updatedObject = new RpslObjectBuilder(MNT_SYNC).append(new RpslAttribute(AttributeType.AUTH, "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/")).get();

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT-SYNC")
                .request(MediaType.APPLICATION_XML)
                .cookie("crowd.token_key", "valid-token")
                .put(Entity.entity(map(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), hasItem(new Attribute("auth", "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/")));
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), hasItem(new Attribute("auth", "SSO person@net.net")));
        assertThat(databaseHelper.lookupObject(ObjectType.MNTNER, "OWNER-MNT-SYNC").findAttributes(AttributeType.AUTH),
                containsInAnyOrder(
                        new RpslAttribute(AttributeType.AUTH, "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test"),
                        new RpslAttribute(AttributeType.AUTH, "SSO 906635c2-0405-429a-800b-0602bd716124"))
        );
    }

    @Test
    public void update_maintainer_sso_with_sync_true_trusted_source_succeeds() {
        try {
            ipRanges.setTrusted("127.0.0.1");
            insertSyncHistory("ORG-TEST", "OWNER-MNT", 1000, false);
            insertSyncHistory("ORG-TEST", "OWNER-MNT", 2000, true);

            final RpslObject updatedObject = new RpslObjectBuilder(OWNER_MNT).append(new RpslAttribute(AttributeType.AUTH, "SSO test@ripe.net")).get();

            final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT")
                    .request(MediaType.APPLICATION_XML)
                    .cookie("crowd.token_key", "valid-token")
                    .put(Entity.entity(map(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class);

            assertThat(whoisResources.getErrorMessages(), is(empty()));
            assertThat(whoisResources.getWhoisObjects(), hasSize(1));
            assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), hasItem(new Attribute("auth", "SSO person@net.net")));
            assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), hasItem(new Attribute("auth", "SSO test@ripe.net")));

            RpslObject rpslObject = databaseHelper.lookupObject(ObjectType.MNTNER, "OWNER-MNT");
            assertThat(rpslObject.findAttributes(AttributeType.AUTH),
                    containsInAnyOrder(
                            new RpslAttribute(AttributeType.AUTH, "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test"),
                            new RpslAttribute(AttributeType.AUTH, "SSO 906635c2-0405-429a-800b-0602bd716124"),
                            new RpslAttribute(AttributeType.AUTH, "SSO 8ffe29be-89ef-41c8-ba7f-0e1553a623e5"))
            );
        } finally {
            ipRanges.setTrusted("::1");
        }
    }

    // helper methods

    private void insertSyncHistory(final String org, final String mntnr,  final long when, final Boolean syncState) {

        final String email = UUID.randomUUID() + "@ripe.net";
        final Timestamp timestamp = new Timestamp(when);

        databaseHelper.getInternalsTemplate().update(
                "INSERT INTO default_maintainer_sync_history (org, mntner, timestamp, email, is_synchronised) VALUES (?, ?, ?, ?, ?)",
                org, mntnr, timestamp, email, syncState);
    }

    private String encode(final String input) {
        // do not interpret template parameters
        return UriComponent.encode(input, UriComponent.Type.QUERY_PARAM, false);
    }

    private WhoisResources map(final RpslObject ... rpslObjects) {
        return whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, rpslObjects);
    }
}
