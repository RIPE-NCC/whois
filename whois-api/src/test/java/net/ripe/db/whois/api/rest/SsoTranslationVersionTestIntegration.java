package net.ripe.db.whois.api.rest;

import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

@Tag("IntegrationTest")
public class SsoTranslationVersionTestIntegration extends AbstractIntegrationTest {
    private static final String MNTNER_VERSION_PATH = "whois/test/mntner/OWNER-MNT/versions/1";
    private static final String AUTH_HASH = "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/";
    private static final String PERSON_UUID = "906635c2-0405-429a-800b-0602bd716124";

    @BeforeAll
    public static void beforeClass() {
        System.setProperty("versions.internal.emails", "person@net.net");
    }

    @AfterAll
    public static void afterClass() {
        System.clearProperty("versions.internal.emails");
    }

    @BeforeEach
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject(
                """
                mntner:      OWNER-MNT
                descr:       Owner Maintainer
                admin-c:     TP1-TEST
                upd-to:      noreply@ripe.net
                auth:        %s #test
                auth:        SSO person@net.net
                mnt-by:      OWNER-MNT
                source:      TEST
                """.formatted(AUTH_HASH));
    }

    @Test
    public void internal_user_sees_translated_sso_in_xml() {
        final WhoisObject whoisObject = getVersion(internal(MNTNER_VERSION_PATH, MediaType.APPLICATION_XML));

        assertThat(whoisObject.getAttributes(), hasItem(new Attribute("auth", AUTH_HASH, "test", null, null, null)));
        assertThat(whoisObject.getAttributes(), hasItem(new Attribute("auth", "SSO person@net.net")));
        assertThat(values(whoisObject, "auth"), not(hasItem("SSO " + PERSON_UUID)));
    }

    @Test
    public void internal_user_sees_translated_sso_in_text_plain() {
        final String response = internal(MNTNER_VERSION_PATH, MediaType.TEXT_PLAIN).get(String.class);

        assertThat(response, containsString("SSO person@net.net"));
        assertThat(response, not(containsString(PERSON_UUID)));
        assertThat(response, not(containsString("# Filtered")));
    }

    @Test
    public void external_user_sees_neither_uuid_nor_email() {
        final String response = external(MNTNER_VERSION_PATH, MediaType.TEXT_PLAIN).get(String.class);

        assertThat(response, containsString("MD5-PW # Filtered"));
        assertThat(response, containsString("SSO # Filtered"));
        assertThat(response, not(containsString(AUTH_HASH)));
        assertThat(response, not(containsString(PERSON_UUID)));
        assertThat(response, not(containsString("person@net.net")));
    }

    @Test
    public void internal_user_sees_translated_sso_in_older_version() {
        databaseHelper.updateObject(
                """
                mntner:      OWNER-MNT
                descr:       Owner Maintainer updated
                admin-c:     TP1-TEST
                upd-to:      noreply@ripe.net
                auth:        %s #test
                auth:        SSO person@net.net
                mnt-by:      OWNER-MNT
                source:      TEST
                """.formatted(AUTH_HASH));

        final String response = internal(MNTNER_VERSION_PATH, MediaType.TEXT_PLAIN).get(String.class);

        assertThat(response, containsString("descr:          Owner Maintainer\n"));
        assertThat(response, containsString("SSO person@net.net"));
        assertThat(response, not(containsString(PERSON_UUID)));
    }

    @Test
    public void internal_user_sees_raw_uuid_when_translation_fails() {
        final String unknownUuid = "test-test-test-super-test";
        databaseHelper.addObject(
                """
                mntner:      UNKNOWN-SSO-MNT
                descr:       untranslatable sso
                admin-c:     TP1-TEST
                upd-to:      noreply@ripe.net
                auth:        SSO %s
                mnt-by:      OWNER-MNT
                source:      TEST
                """.formatted(unknownUuid));

        final String response = internal("whois/test/mntner/UNKNOWN-SSO-MNT/versions/1", MediaType.TEXT_PLAIN).get(String.class);

        assertThat(response, containsString("SSO " + unknownUuid));
        assertThat(response, not(containsString("# Filtered")));
    }

    private Invocation.Builder internal(final String path, final String mediaType) {
        return RestTest.target(getPort(), path)
                .request(mediaType)
                .cookie("crowd.token_key", "valid-token");
    }

    private Invocation.Builder external(final String path, final String mediaType) {
        return RestTest.target(getPort(), path)
                .request(mediaType);
    }

    private WhoisObject getVersion(final Invocation.Builder request) {
        final WhoisResources whoisResources = request.get(WhoisResources.class);
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        return whoisResources.getWhoisObjects().getFirst();
    }

    private static List<String> values(final WhoisObject whoisObject, final String name) {
        return whoisObject.getAttributes().stream()
                .filter(attribute -> attribute.getName().equals(name))
                .map(Attribute::getValue)
                .toList();
    }
}