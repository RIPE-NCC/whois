package net.ripe.db.whois.api.rest;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.TestDateTimeProvider;
import net.ripe.db.whois.common.domain.User;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("IntegrationTest")
public class OverrideWithNonTrustedIpTestIntegration extends AbstractIntegrationTest {

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

    private static final RpslObject TEST_PERSON = RpslObject.parse("" +
            "person:    Test Person\n" +
            "address:   Singel 258\n" +
            "phone:     +31 6 12345678\n" +
            "nic-hdl:   TP1-TEST\n" +
            "mnt-by:    OWNER-MNT\n" +
            "source:    TEST\n");


    @Autowired private WhoisObjectMapper whoisObjectMapper;
    @Autowired private TestDateTimeProvider testDateTimeProvider;

    @BeforeEach
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");

        databaseHelper.insertUser(User.createWithPlainTextPassword("db_e2e_1", "zoh", ObjectType.PERSON));
        databaseHelper.insertUser(User.createWithPlainTextPassword("person", "zoh", ObjectType.PERSON));

        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TEST_PERSON);
        testDateTimeProvider.setTime(LocalDateTime.parse("2001-02-04T17:00:00"));
    }

    @BeforeAll
    public static void beforeClass() {
        System.setProperty("ipranges.trusted", "");
    }

    @AfterAll
    public static void clearProperty() {
        System.clearProperty("ipranges.trusted");
    }

    @Test
    public void create_person_with_override_no_sso_fails() {
        try {
            RestTest.target(getPort(), "whois/test/person")
                    .queryParam("override", "person,zoh,reason")
                    .request()
                    .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);

            fail();
        } catch (NotAuthorizedException ex) {
            final WhoisResources whoisResources = ex.getResponse().readEntity(WhoisResources.class);
            RestTest.assertErrorCount(whoisResources, 1);
            RestTest.assertErrorMessage(whoisResources, 0, "Error", "Override only allowed by database administrators");
            assertThat(whoisResources.getTermsAndConditions().getHref(), is(WhoisResources.TERMS_AND_CONDITIONS));
        }
    }

    @Test
    public void create_person_with_override_with_sso_wrong_user_fails() {
        try {
            RestTest.target(getPort(), "whois/test/person")
                    .queryParam("override", "person,zoh,reason")
                    .request(MediaType.APPLICATION_XML)
                    .cookie("crowd.token_key", "db_e2e_1")
                    .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);

            fail();
        } catch (NotAuthorizedException ex) {
            final WhoisResources whoisResources = ex.getResponse().readEntity(WhoisResources.class);
            RestTest.assertErrorCount(whoisResources, 1);
            RestTest.assertErrorMessage(whoisResources, 0, "Error", "Override only allowed by database administrators");
            assertThat(whoisResources.getTermsAndConditions().getHref(), is(WhoisResources.TERMS_AND_CONDITIONS));
        }
    }

    @Test
    public void create_person_with_override_with_sso_wrong_domain_fails() {
        try {
            RestTest.target(getPort(), "whois/test/person")
                    .queryParam("override", "person,zoh,reason")
                    .request(MediaType.APPLICATION_XML)
                    .cookie("crowd.token_key", "valid-token")
                    .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);

            fail();
        } catch (NotAuthorizedException ex) {
            final WhoisResources whoisResources = ex.getResponse().readEntity(WhoisResources.class);
            RestTest.assertErrorCount(whoisResources, 1);
            RestTest.assertErrorMessage(whoisResources, 0, "Error", "Override only allowed by database administrators");
            assertThat(whoisResources.getTermsAndConditions().getHref(), is(WhoisResources.TERMS_AND_CONDITIONS));
        }
    }

    @Test
    public void create_person_with_override_with_sso_succeeds() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person")
                .queryParam("override", encode("db_e2e_1,zoh,reason {notify=false}"))
                .request()
                .cookie("crowd.token_key", "db_e2e_1")
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);

        final WhoisObject object = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisResources.getErrorMessages().get(0).getText(), is("Authorisation override used"));
        assertThat(object.getType(), is("person"));
        assertThat(object.getPrimaryKey(), contains(new Attribute("nic-hdl", "PP1-TEST")));
    }

    @Test
    public void delete_person_with_override_with_sso_succeeds() {
        databaseHelper.addObject(RpslObject.parse("" +
                "person:    Test Person2\n" +
                "address:   Singel 258\n" +
                "phone:     +31 6 12345678\n" +
                "nic-hdl:   TP2-TEST\n" +
                "mnt-by:    OWNER-MNT\n" +
                "source:    TEST\n"));

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/TP2-TEST")
                .queryParam("override", encode("db_e2e_1,zoh,reason {notify=false}"))
                .request()
                .cookie("crowd.token_key", "db_e2e_1")
                .delete(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages().get(0).getText(), is("Authorisation override used"));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        try {
            databaseHelper.lookupObject(ObjectType.PERSON, "TP2-TEST");
            fail();
        } catch (EmptyResultDataAccessException ignored) {
            // expected
        }
    }

    @Test
    public void update_succeeds() {
        final RpslObject updatedObject = new RpslObjectBuilder(TEST_PERSON).append(new RpslAttribute(AttributeType.REMARKS, "updated")).sort().get();

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                .queryParam("override", encode("db_e2e_1,zoh,reason {notify=false}"))
                .request(MediaType.APPLICATION_XML)
                .cookie("crowd.token_key", "db_e2e_1")
                .put(Entity.entity(map(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class);


        assertThat(whoisResources.getErrorMessages().get(0).getText(), is("Authorisation override used"));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject object = whoisResources.getWhoisObjects().get(0);
        assertThat(object.getPrimaryKey(), contains(new Attribute("nic-hdl", "TP1-TEST")));

        assertThat(whoisResources.getTermsAndConditions().getHref(), is(WhoisResources.TERMS_AND_CONDITIONS));
    }

    private WhoisResources map(final RpslObject ... rpslObjects) {
        return whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, rpslObjects);
    }

}
