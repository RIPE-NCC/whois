package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.HttpHeaders;
import net.ripe.db.whois.api.SecureRestTest;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.syncupdate.SyncUpdateUtils;
import net.ripe.db.whois.common.domain.User;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("IntegrationTest")
public class WhoisRestOidcServiceEndToEndTestIntegration extends WhoisRestServiceEndToEndTestIntegration{

    @BeforeAll
    public static void setupApiProperties() {
        System.setProperty("oidc.auth.enable","true");
        System.setProperty("oidc.session.client.id", APP_CLIENT_ID);
    }

    @AfterAll
    public static void restApiProperties() {
        System.clearProperty("oidc.auth.enable");
        System.clearProperty("oidc.session.client.id");
    }

    @Test
    @Override
    public void create_assignment__mntby_SSO_no_pw__invalid_SSO_token() {
        databaseHelper.addObjects(
                makeMntner("LIR", "auth: SSO " + USER1),
                makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT"));

        final RpslObject assignment = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT");

        try {
            getWebTarget("whois/test/inetnum", "deadbeef", mediaType)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, assignment), mediaType), WhoisResources.class);
            fail();
        } catch (NotAuthorizedException expected) {
            final WhoisResources whoisResources = expected.getResponse().readEntity(WhoisResources.class);
            final ErrorMessage errorMessage = Lists.reverse(whoisResources.getErrorMessages()).getFirst();
            assertThat(errorMessage.getText(), is("Invalid Bearer Token"));
        }
    }

    @Test
    @Override
    public void on_exception_remove_password_in_link_when_override_is_not_used() {
        databaseHelper.addObjects(
                makeMntner("LIR", "auth: SSO " + USER1),
                makeMntner("LIR2", "auth: SSO " + USER2),
                makeMntner("LIR3", "auth: MD5-PW $1$7AEhjSjo$KvxW0YOJFkHpoZqBkpTiO0 # lir"),
                makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT"));

        final RpslObject assignment = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT", "mnt-by: LIR2-MNT", "mnt-by: LIR3-MNT", "changed: john.smith@example.com 20171114");

        try {
            final WhoisResources whoisResources = getWebTarget("whois/test/inetnum?password=owner", "db_e2e_2", mediaType)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, assignment), mediaType), WhoisResources.class);

            assertThat(whoisResources.getLink().getHref(), is("https://localhost:" + getSecurePort() + "/test/inetnum"));

        } catch (ClientErrorException e) {
            reportAndThrowUnknownError(e);
        }
    }

    @Test
    @Override
    public void on_exception_filter_password_in_link_when_override_is_used() {
        databaseHelper.insertUser(User.createWithPlainTextPassword("personadmin", OVERRIDE_PASSWORD, ObjectType.values()));
        databaseHelper.addObjects(
                makeMntner("LIR", "auth: SSO " + USER1),
                makeMntner("LIR2", "auth: SSO " + USER2),
                makeMntner("LIR3", "auth: MD5-PW $1$7AEhjSjo$KvxW0YOJFkHpoZqBkpTiO0 # lir"),
                makeInetnum("10.0.0.0 - 10.255.255.255", "mnt-lower: OWNER-MNT"));

        final RpslObject assignment = makeInetnum("10.0.0.0 - 10.0.255.255", "status: ASSIGNED PA", "mnt-by: LIR-MNT", "mnt-by: LIR2-MNT", "mnt-by: LIR3-MNT", "changed: john.smith@example.com 20171114");

        try {
            final WhoisResources whoisResources =
                    getWebTarget("whois/test/inetnum?override=" + SyncUpdateUtils.encode("personadmin," + OVERRIDE_PASSWORD + ",reason"), "db_e2e_2", mediaType)
                            .post(Entity.entity(whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, assignment), mediaType), WhoisResources.class);

            assertThat(whoisResources.getLink().getHref(), is("https://localhost:" + getSecurePort() + "/test/inetnum?override=personadmin,FILTERED,reason"));
        } catch (ClientErrorException e) {
            reportAndThrowUnknownError(e);
        }
    }


    @Override
    Invocation.Builder getWebTarget(final String path, String authValue,
                                    final String mediaType) {
        return SecureRestTest.target(getSecurePort(), path)
                .request(mediaType)
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenForOidc(authValue));
    }
}
