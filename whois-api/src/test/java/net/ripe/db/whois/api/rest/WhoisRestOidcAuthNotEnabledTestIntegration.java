package net.ripe.db.whois.api.rest;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.whois.api.SecureRestTest;
import net.ripe.db.whois.api.httpserver.AbstractHttpsIntegrationTest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static net.ripe.db.whois.api.rest.WhoisRestOidcAuthTokenIntrospectionTestIntegration.APP_CLIENT_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThrows;

public class WhoisRestOidcAuthNotEnabledTestIntegration extends AbstractHttpsIntegrationTest {

    @Autowired
    private WhoisObjectMapper whoisObjectMapper;

    static final RpslObject TEST_PERSON = RpslObject.parse("" +
            "person:         Test Person\n" +
            "e-mail:         test@ripe.net\n" +
            "address:        Singel 258\n" +
            "phone:          +31 6 12345678\n" +
            "nic-hdl:        TP1-TEST\n" +
            "mnt-by:         OWNER-MNT\n" +
            "source:         TEST\n");

    static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:      OWNER-MNT\n" +
            "descr:       Owner Maintainer\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "auth:        SSO person@net.net\n" +
            "mnt-by:      OWNER-MNT\n" +
            "source:      TEST");

    static final RpslObject PAULETH_PALTHEN = RpslObject.parse("" +
            "person:    Pauleth Palthen\n" +
            "address:   Singel 258\n" +
            "phone:     +31-1234567890\n" +
            "e-mail:    noreply@ripe.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "nic-hdl:   PP1-TEST\n" +
            "remarks:   remark\n" +
            "source:    TEST\n");

    @BeforeAll
    public static void setupApiProperties() {
        System.setProperty("oidc.auth.enable","false");
        System.setProperty("oidc.session.client.id", APP_CLIENT_ID);

        System.setProperty("apikey.max.scope","2");
    }

    @AfterAll
    public static void restApiProperties() {
        System.clearProperty("oidc.auth.enable");
        System.clearProperty("oidc.session.client.id");
        System.clearProperty("apikey.max.scope");
    }

    @BeforeEach
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TEST_PERSON);
        testDateTimeProvider.setTime(LocalDateTime.parse("2001-02-04T17:00:00"));
    }

    @Test
    public void update_person_using_oidc_fails() {
        databaseHelper.addObject(PAULETH_PALTHEN);
        final RpslObject updatedObject = new RpslObjectBuilder(PAULETH_PALTHEN).append(new RpslAttribute(AttributeType.REMARKS, "updated")).sort().get();

        final NotAuthorizedException notAuthorizedException = assertThrows(NotAuthorizedException.class, () ->
                SecureRestTest.target(getSecurePort(), "whois/test/person/PP1-TEST")
                        .request(MediaType.APPLICATION_XML)
                        .header(HttpHeaders.AUTHORIZATION, getBearerTokenForOidc("valid-token"))
                        .put(Entity.entity(map(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class)
        );

        final WhoisResources whoisResources = notAuthorizedException.getResponse().readEntity(WhoisResources.class);
        assertThat(whoisResources.getErrorMessages(), hasSize(2));
        assertThat(whoisResources.getErrorMessages().getFirst().toString(), containsString("Authorisation for [person] PP1-TEST failed"));
        assertThat(whoisResources.getErrorMessages().get(1).toString(), containsString("Wrong whois scope."));
    }

    WhoisResources map(final RpslObject... rpslObjects) {
        return whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, rpslObjects);
    }
}
