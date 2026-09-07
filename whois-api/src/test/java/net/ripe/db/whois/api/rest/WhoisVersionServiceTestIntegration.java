package net.ripe.db.whois.api.rest;

import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.domain.WhoisVersion;
import net.ripe.db.whois.api.rest.domain.WhoisVersions;
import net.ripe.db.whois.api.rest.domain.Version;
import net.ripe.db.whois.common.ApplicationVersion;

import jakarta.ws.rs.core.HttpHeaders;
import net.ripe.db.whois.api.SecureRestTest;
import net.ripe.db.whois.api.httpserver.AbstractHttpsIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_EXPIRED;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_PERSON_ANY_MNT;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_TEST_NO_MNT;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import net.ripe.db.whois.common.MaintenanceMode;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import java.time.LocalDateTime;
import java.util.List;

import static net.ripe.db.whois.common.support.StringMatchesRegexp.stringMatchesRegexp;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;


@Tag("IntegrationTest")
public class WhoisVersionServiceTestIntegration extends AbstractHttpsIntegrationTest {
    @Autowired
    private ApplicationVersion applicationVersion;

    private static final String MNTNER_VERSION_PATH = "whois/test/mntner/OWNER-MNT/versions/1";
    private static final String AUTNUM_VERSION_PATH = "whois/test/aut-num/AS102/versions/1";
    private static final String AUTH_HASH = "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/";

    private static final RpslObject OWNER_MNT = RpslObject.parse(
    """
    mntner:      OWNER-MNT
    descr:       Owner Maintainer
    admin-c:     TP1-TEST
    upd-to:      noreply@ripe.net
    auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test
    auth:        SSO person@net.net
    mnt-by:      OWNER-MNT
    source:      TEST
    """);

    private static final RpslObject TEST_PERSON = RpslObject.parse(
    """
    person:    Test Person
    address:   Singel 258
    phone:     +31 6 12345678
    nic-hdl:   TP1-TEST
    mnt-by:    OWNER-MNT
    source:    TEST
    """);

    private static final String VERSION_DATE_PATTERN = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z";

    private void assertApplicationVersion(final Version version) {
        assertThat(version, is(notNullValue()));
        assertThat(version.getVersion(), is(applicationVersion.getVersion()));
        assertThat(version.getTimestamp(), is(applicationVersion.getTimestamp()));
        assertThat(version.getCommitId(), is(applicationVersion.getCommitId()));
    }

    @Autowired
    private MaintenanceMode maintenanceMode;

    @BeforeAll
    public static void beforeClass() {
        System.setProperty("versions.internal.emails", "person@net.net");
        System.setProperty("oidc.auth.enable", "true");
        System.setProperty("oidc.session.client.id", APP_CLIENT_ID);
    }

    @AfterAll
    public static void afterClass() {
        System.clearProperty("versions.internal.emails");
        System.clearProperty("oidc.auth.enable");
        System.clearProperty("oidc.session.client.id");
    }

    @BeforeEach
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject("role: Test Role\nnic-hdl: TR1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TEST_PERSON);
        maintenanceMode.set("FULL,FULL");
        testDateTimeProvider.setTime(LocalDateTime.parse("2001-02-04T17:00:00"));
    }

    @Test
    public void versions_returns_xml() {
        databaseHelper.addObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        final WhoisVersions whoisVersions = whoisResources.getVersions();
        assertThat(whoisVersions.getType(), is("aut-num"));
        assertThat(whoisVersions.getKey(), is("AS102"));
        assertThat(whoisVersions.getVersions(), hasSize(1));
        final WhoisVersion whoisVersion = whoisVersions.getVersions().getFirst();
        assertThat(whoisVersion, is(new WhoisVersion("ADD/UPD", whoisVersion.getDate(), 1)));
    }

    @Test
    public void versions_deleted() {
        final RpslObject autnum = RpslObject.parse(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);
        databaseHelper.addObject(autnum);
        databaseHelper.deleteObject(autnum);
        databaseHelper.addObject(autnum);
        databaseHelper.updateObject(
                """
                aut-num:        AS102
                as-name:        End-User-3
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);
        databaseHelper.deleteObject(autnum);
        databaseHelper.addObject(autnum);
        databaseHelper.updateObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);
        assertThat(whoisResources.getErrorMessages(), is(empty()));

        final List<WhoisVersion> versions = whoisResources.getVersions().getVersions();
        assertThat(versions, hasSize(2));

        assertThat(versions.getFirst().getDeletedDate(), is(nullValue()));
        assertThat(versions.getFirst().getOperation(), is("ADD/UPD"));
        assertThat(versions.getFirst().getRevision(), is(1));
        assertThat(versions.getFirst().getDate(), stringMatchesRegexp(VERSION_DATE_PATTERN));

        assertThat(versions.get(1).getDeletedDate(), is(nullValue()));
        assertThat(versions.get(1).getOperation(), is("ADD/UPD"));
        assertThat(versions.get(1).getRevision(), is(2));
        assertThat(versions.get(1).getDate(), stringMatchesRegexp(VERSION_DATE_PATTERN));
    }

    @Test
    public void versions_xml_contains_application_version() {
        databaseHelper.addObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertApplicationVersion(whoisResources.getVersion());
    }

    @Test
    public void versions_json_contains_application_version() {
        databaseHelper.addObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions")
                .request(MediaType.APPLICATION_JSON)
                .get(WhoisResources.class);

        assertApplicationVersion(whoisResources.getVersion());
    }

    @Test
    public void version_xml_contains_application_version() {
        databaseHelper.addObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/1")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertApplicationVersion(whoisResources.getVersion());
    }

    @Test
    public void versions_text_plain_has_no_application_version() {
        databaseHelper.addObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);

        final String response = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions")
                .request(MediaType.TEXT_PLAIN)
                .get(String.class);

        assertThat(response, not(containsString("commit-id")));
        assertThat(response, containsString("Version history for AUT-NUM object \"AS102\""));
    }

    @Test
    public void version_text_plain_has_no_application_version() {
        databaseHelper.addObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);

        final String response = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/1")
                .request(MediaType.TEXT_PLAIN)
                .get(String.class);

        assertThat(response, is(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                mnt-by:         OWNER-MNT
                source:         TEST
                """));
    }

    @Test
    public void version_json_contains_application_version() {
        databaseHelper.addObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/1")
                .request(MediaType.APPLICATION_JSON)
                .get(WhoisResources.class);

        assertApplicationVersion(whoisResources.getVersion());
    }

    @Test
    public void version_and_lookup_report_same_application_version() {
        databaseHelper.addObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);

        final Version fromVersion = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/1")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class)
                .getVersion();

        final Version fromLookup = RestTest.target(getPort(), "whois/test/aut-num/AS102")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class)
                .getVersion();

        assertThat(fromVersion.getVersion(), is(fromLookup.getVersion()));
        assertThat(fromVersion.getTimestamp(), is(fromLookup.getTimestamp()));
        assertThat(fromVersion.getCommitId(), is(fromLookup.getCommitId()));
    }

    @Test
    public void versions_deleted_versions_json() {
        final RpslObject autnum = RpslObject.parse(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);
        databaseHelper.addObject(autnum);
        databaseHelper.deleteObject(autnum);
        databaseHelper.addObject(autnum);
        databaseHelper.updateObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions")
                .request(MediaType.APPLICATION_JSON)
                .get(WhoisResources.class);
        assertThat(whoisResources.getErrorMessages(), is(empty()));

        final List<WhoisVersion> versions = whoisResources.getVersions().getVersions();
        assertThat(versions, hasSize(2));

        assertThat(versions.getFirst().getDeletedDate(), is(nullValue()));
        assertThat(versions.getFirst().getOperation(), is("ADD/UPD"));
        assertThat(versions.getFirst().getRevision(), is(1));
        assertThat(versions.getFirst().getDate(), stringMatchesRegexp(VERSION_DATE_PATTERN));

        assertThat(versions.get(1).getDeletedDate(), is(nullValue()));
        assertThat(versions.get(1).getOperation(), is("ADD/UPD"));
        assertThat(versions.get(1).getRevision(), is(2));
        assertThat(versions.get(1).getDate(), stringMatchesRegexp(VERSION_DATE_PATTERN));
    }

    @Test
    public void versions_return_text() {
        databaseHelper.addObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);

        final String response = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions")
                .request(MediaType.TEXT_PLAIN)
                .get(String.class);

        assertThat(response, containsString("Version history for AUT-NUM object \"AS102\""));
        assertThat(response, containsString("rev#  Date"));
        assertThat(response, containsString("ADD/UPD"));
        assertThat(response, stringMatchesRegexp("(?s).*1\\s+" + VERSION_DATE_PATTERN + "\\s+ADD/UPD.*"));
    }

    @Test
    public void versions_return_text_with_txt_extension() {
        databaseHelper.addObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);

        final String response = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions.txt")
                .request()
                .get(String.class);

        assertThat(response, containsString("Version history for AUT-NUM object \"AS102\""));
        assertThat(response, containsString("rev#  Date"));
        assertThat(response, containsString("ADD/UPD"));
        assertThat(response, stringMatchesRegexp("(?s).*1\\s+" + VERSION_DATE_PATTERN + "\\s+ADD/UPD.*"));
    }

    @Test
    public void versions_text_header_format() {
        databaseHelper.addObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);

        final String response = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions")
                .request(MediaType.TEXT_PLAIN)
                .get(String.class);

        assertThat(response, containsString("Version history for AUT-NUM object \"AS102\""));
        assertThat(response, containsString("rev#  Date"));
        assertThat(response, containsString("Op."));
    }

    @Test
    public void versions_text_single_version_row() {
        databaseHelper.addObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);

        final String response = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions")
                .request(MediaType.TEXT_PLAIN)
                .get(String.class);

        assertThat(response, stringMatchesRegexp("(?s).*1\\s+" + VERSION_DATE_PATTERN + "\\s+ADD/UPD.*"));
    }

    @Test
    public void versions_text_multiple_versions_listed() {
        databaseHelper.addObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);
        databaseHelper.updateObject(
                """
                aut-num:        AS102
                as-name:        End-User-3
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);

        final String response = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions")
                .request(MediaType.TEXT_PLAIN)
                .get(String.class);

        assertThat(response, stringMatchesRegexp("(?s).*1\\s+" + VERSION_DATE_PATTERN + "\\s+ADD/UPD.*"));
        assertThat(response, stringMatchesRegexp("(?s).*2\\s+" + VERSION_DATE_PATTERN + "\\s+ADD/UPD.*"));
    }

    @Test
    public void versions_text_excludes_leading_deleted_version() {
        final RpslObject autnum = RpslObject.parse(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);
        databaseHelper.addObject(autnum);
        databaseHelper.deleteObject(autnum);
        databaseHelper.addObject(autnum);

        final String response = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions")
                .request(MediaType.TEXT_PLAIN)
                .get(String.class);

        assertThat(response, stringMatchesRegexp("(?s).*1\\s+" + VERSION_DATE_PATTERN + "\\s+ADD/UPD.*"));
        assertThat(response, containsString("Version history for AUT-NUM object \"AS102\""));
    }

    @Test
    public void versions_text_no_versions_found_returns_not_found() {
        assertThrows(NotFoundException.class, () -> RestTest.target(getPort(), "whois/test/aut-num/AS102/versions")
                .request(MediaType.TEXT_PLAIN)
                .get(String.class));
    }

    @Test
    public void versions_last_version_deleted() {
        final RpslObject autnum = RpslObject.parse(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);
        databaseHelper.addObject(autnum);
        databaseHelper.deleteObject(autnum);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);
        assertThat(whoisResources.getErrorMessages(), is(empty()));

        final List<WhoisVersion> versions = whoisResources.getVersions().getVersions();
        assertThat(versions, hasSize(0));
    }

    @Test
    public void versions_no_versions_found() {
        assertThrows(NotFoundException.class, () -> RestTest.target(getPort(), "whois/test/aut-num/AS102/versions")
                .request(MediaType.APPLICATION_XML)
                .get(String.class));
    }

    @Test
    public void version_returns_text() {
        databaseHelper.addObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);

        final String response = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/1")
                .request(MediaType.TEXT_PLAIN)
                .get(String.class);

        assertThat(response, is(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                mnt-by:         OWNER-MNT
                source:         TEST
                """));
    }

    @Test
    public void version_returns_text_with_txt_extension() {
        databaseHelper.addObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);

        final String response = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/1.txt")
                .request()
                .get(String.class);

        assertThat(response, is(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                mnt-by:         OWNER-MNT
                source:         TEST
                """));
    }

    @Test
    public void version_text_contains_all_attributes() {
        databaseHelper.addObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);

        final String response = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/1")
                .request(MediaType.TEXT_PLAIN)
                .get(String.class);

        assertThat(response, containsString("aut-num:        AS102"));
        assertThat(response, containsString("as-name:        End-User-2"));
        assertThat(response, containsString("descr:          description"));
        assertThat(response, containsString("mnt-by:         OWNER-MNT"));
        assertThat(response, containsString("source:         TEST"));
    }

    @Test
    public void version_text_returns_correct_revision() {
        databaseHelper.addObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);
        databaseHelper.updateObject(
                """
                aut-num:        AS102
                as-name:        End-User-3
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);

        final String version1 = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/1")
                .request(MediaType.TEXT_PLAIN)
                .get(String.class);
        final String version2 = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/2")
                .request(MediaType.TEXT_PLAIN)
                .get(String.class);

        assertThat(version1, containsString("as-name:        End-User-2"));
        assertThat(version2, containsString("as-name:        End-User-3"));
    }

    @Test
    public void version_text_nonexistent_version_returns_not_found() {
        databaseHelper.addObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);

        assertThrows(NotFoundException.class, () -> RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/2")
                .request(MediaType.TEXT_PLAIN)
                .get(String.class));
    }

    @Test
    public void version_text_wrong_object_type_returns_not_found() {
        databaseHelper.addObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);

        assertThrows(NotFoundException.class, () -> RestTest.target(getPort(), "whois/test/inetnum/AS102/versions/1")
                .request(MediaType.TEXT_PLAIN)
                .get(String.class));
    }

    @Test
    public void version_text_deleted_version_returns_not_found() {
        final RpslObject autnum = RpslObject.parse(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);
        databaseHelper.addObject(autnum);
        databaseHelper.deleteObject(autnum);

        assertThrows(NotFoundException.class, () -> RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/1")
                .request(MediaType.TEXT_PLAIN)
                .get(String.class));
    }

    @Test
    public void version_nonexistant_version() {
        databaseHelper.addObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);

        assertThrows(NotFoundException.class, () -> RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/2")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class));
    }

    @Test
    public void version_wrong_object_type() {
        databaseHelper.addObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);

        assertThrows(NotFoundException.class, () -> RestTest.target(getPort(), "whois/test/inetnum/AS102/versions/1")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class));
    }

    @Test
    public void version_returns_xml() {
        databaseHelper.addObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/1")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject object = whoisResources.getWhoisObjects().getFirst();
        assertThat(object.getType(), is("aut-num"));
        assertThat(object.getVersion(), is(1));

        final List<Attribute> attributes = object.getAttributes();
        final List<RpslAttribute> originalAttributes =  RpslObject.parse(
                        """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                mnt-by:         OWNER-MNT
                source:         TEST
                """)
                .getAttributes();

        for (int i = 0; i < originalAttributes.size(); i++) {
            assertThat(originalAttributes.get(i).getCleanValue().toString(), is(attributes.get(i).getValue()));
        }
    }

    @Test
    public void version_returns_json() {
        databaseHelper.addObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                e-mail:          test@test.nl
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                notify:         notify@me.nl
                source:         TEST
                """);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/1")
                .request(MediaType.APPLICATION_JSON)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject object = whoisResources.getWhoisObjects().getFirst();
        assertThat(object.getType(), is("aut-num"));
        assertThat(object.getVersion(), is(1));

        final List<Attribute> attributes = object.getAttributes();

        final List<RpslAttribute> originalAttributes = RpslObject.parse(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                mnt-by:         OWNER-MNT
                source:         TEST
                """)
                .getAttributes();

        for (int i = 0; i < originalAttributes.size(); i++) {
            assertThat(originalAttributes.get(i).getCleanValue().toString(), is(attributes.get(i).getValue()));
        }
    }

    @Test
    public void version_not_showing_deleted_version() {
        final RpslObject autnum = RpslObject.parse(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);
        databaseHelper.addObject(autnum);
        databaseHelper.deleteObject(autnum);

        assertThrows(NotFoundException.class, () -> RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/1")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class));
    }

    @Test
    public void version_returns_formatted() {
        databaseHelper.addObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                e-mail:          test@test.nl
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                notify:         notify@me.nl
                source:         TEST
                """);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/1?unformatted=false")
                .request()
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().getFirst();
        final List<RpslAttribute> originalAttributes = RpslObject.parse(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                mnt-by:         OWNER-MNT
                source:         TEST
                """)
                .getAttributes();
        final List<Attribute> attributes = whoisObject.getAttributes();

        assertThat(attributes, hasSize(originalAttributes.size()));

        for (int i = 0; i < originalAttributes.size(); i++) {
            assertThat(originalAttributes.get(i).getCleanValue().toString(), is(attributes.get(i).getValue()));
        }
    }

    @Test
    public void version_returns_unformatted() {
        databaseHelper.addObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description1
                                description2
                mnt-by:         OWNER-MNT
                source:         TEST
                """);

        final WhoisResources whoisResources = RestTest.target(getPort(),
                        "whois/test/aut-num/AS102/versions/1?unformatted=true")
                .request()
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final List<Attribute> attributes = whoisResources.getWhoisObjects().getFirst().getAttributes();

        assertThat(attributes.get(0).getValue(), is("        AS102"));
        assertThat(attributes.get(1).getValue(), is("        End-User-2"));
        assertThat(attributes.get(2).getValue(), is(
                "          description1\n" +
                        "                description2"));
    }

    @Test
    public void lookup_non_streaming_puts_xlink_into_root_element_and_nowhere_else() {
        databaseHelper.addObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """);

        final String whoisResources = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/1").request(MediaType.APPLICATION_XML).get(String.class);

        assertThat(whoisResources, containsString("<whois-resources xmlns:xlink=\"http://www.w3.org/1999/xlink\">"));
        assertThat(whoisResources, containsString("<object type=\"aut-num\" version=\"1\">"));
        assertThat(whoisResources, containsString("<objects>"));
    }

    @Test
    public void lookup_role_text_plain_extension_utf8() {
        final RpslObject inetnumV1 = RpslObject.parse(
                """
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       OWNER-MNT
                remarks:      version 第二
                source:       TEST
                """);

        final RpslObject inetnumV2 = RpslObject.parse(
                """
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       OWNER-MNT
                remarks:      version 第三
                source:       TEST
                """);

        databaseHelper.addObject(inetnumV1);
        databaseHelper.updateObject(inetnumV2);

        final Response response = RestTest.target(getPort(), "whois/test/inetnum/192.168.0.0 - 192.169.255.255/versions/2")
                .request()
                .get(Response.class);

        final String rpslObject = response.readEntity(String.class);
        assertThat(rpslObject, containsString("version 第三"));
    }

    @Test
    public void external_user_gets_filtered_mntner_version() {
        final WhoisObject whoisObject = getVersion(external(MNTNER_VERSION_PATH, MediaType.APPLICATION_XML));

        assertThat(whoisObject.getAttributes(), hasItem(new Attribute("auth", "MD5-PW", "Filtered", null, null, null)));
        assertThat(values(whoisObject, "upd-to"), hasSize(0));
        assertThat(values(whoisObject, "notify"), hasSize(0));
        assertThat(values(whoisObject, "admin-c"), hasSize(0));
        assertThat(attribute(whoisObject).getComment(), is("Filtered"));
    }

    @Test
    public void internal_user_gets_unfiltered_mntner_version_json() {
        final WhoisObject whoisObject = getVersion(internal(MNTNER_VERSION_PATH, MediaType.APPLICATION_JSON));

        assertThat(whoisObject.getAttributes(), hasItem(new Attribute("auth", AUTH_HASH, "test", null, null, null)));
        assertThat(whoisObject.getAttributes(), hasItem(new Attribute("upd-to", "noreply@ripe.net")));
        assertThat(values(whoisObject, "admin-c"), hasItem("TP1-TEST"));
    }

    @Test
    public void internal_user_gets_unfiltered_mntner_version_text_plain() {
        final String response = internal(MNTNER_VERSION_PATH, MediaType.TEXT_PLAIN).get(String.class);

        assertThat(response, containsString(AUTH_HASH));
        assertThat(response, containsString("upd-to:"));
        assertThat(response, containsString("admin-c:"));
        assertThat(response, not(containsString("# Filtered")));
    }

    @Test
    public void external_user_gets_filtered_mntner_version_text_plain() {
        final String response = external(MNTNER_VERSION_PATH, MediaType.TEXT_PLAIN).get(String.class);

        assertThat(response, not(containsString(AUTH_HASH)));
        assertThat(response, containsString("MD5-PW # Filtered"));
        assertThat(response, not(containsString("upd-to:")));
        assertThat(response, not(containsString("admin-c:")));
    }

    @Test
    public void internal_user_gets_unfiltered_autnum_version_text_plain() {
        databaseHelper.addObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                e-mail:         test@test.nl
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                notify:         notify@me.nl
                source:         TEST
                """);

        final String response = internal(AUTNUM_VERSION_PATH, MediaType.TEXT_PLAIN).get(String.class);

        assertThat(response, containsString("e-mail:         test@test.nl"));
        assertThat(response, containsString("notify:         notify@me.nl"));
        assertThat(response, containsString("admin-c:        TP1-TEST"));
        assertThat(response, containsString("tech-c:         TP1-TEST"));
    }

    @Test
    public void external_user_gets_filtered_autnum_version_text_plain() {
        databaseHelper.addObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                e-mail:         test@test.nl
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                notify:         notify@me.nl
                source:         TEST
                """);

        final String response = external(AUTNUM_VERSION_PATH, MediaType.TEXT_PLAIN).get(String.class);

        assertThat(response, is(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                mnt-by:         OWNER-MNT
                source:         TEST # Filtered
                """));
    }

    @Test
    public void sso_session_without_allow_list_entry_gets_filtered_version() {
        final String response = RestTest.target(getPort(), MNTNER_VERSION_PATH)
                .request(MediaType.TEXT_PLAIN)
                .cookie("crowd.token_key", "db_e2e_1")
                .get(String.class);

        assertThat(response, containsString("MD5-PW # Filtered"));
    }

    @Test
    public void allowed_sso_session_from_untrusted_address_gets_filtered_version() {
        final String response = RestTest.target(getPort(), MNTNER_VERSION_PATH + "?clientIp=2001:fff:001::")
                .request(MediaType.TEXT_PLAIN)
                .cookie("crowd.token_key", "valid-token")
                .get(String.class);

        assertThat(response, containsString("MD5-PW # Filtered"));
    }

    @Test
    public void invalid_sso_token_gets_filtered_version() {
        final String response = RestTest.target(getPort(), MNTNER_VERSION_PATH)
                .request(MediaType.TEXT_PLAIN)
                .cookie("crowd.token_key", "invalid-token")
                .get(String.class);

        assertThat(response, containsString("MD5-PW # Filtered"));
    }

    @Test
    public void no_sso_session_gets_filtered_version() {
        final String response = external(MNTNER_VERSION_PATH, MediaType.TEXT_PLAIN).get(String.class);

        assertThat(response, containsString("MD5-PW # Filtered"));
    }

    @Test
    public void version_list_works_for_external_user() {
        final WhoisResources whoisResources = external("whois/test/mntner/OWNER-MNT/versions", MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getVersions().getVersions(), hasSize(1));
    }

    @Test
    public void version_list_works_for_internal_user() {
        final WhoisResources whoisResources = internal("whois/test/mntner/OWNER-MNT/versions", MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getVersions().getVersions(), hasSize(1));
    }

    @Test
    public void internal_user_gets_unfiltered_mntner_version() {
        final WhoisObject whoisObject = getVersion(internal("whois/test/mntner/OWNER-MNT/versions/1", MediaType.APPLICATION_XML));

        assertThat(whoisObject.getAttributes(), hasItem(new Attribute("auth", "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/", "test", null, null, null)));
        assertThat(whoisObject.getAttributes(), hasItem(new Attribute("auth", "SSO person@net.net")));
        assertThat(whoisObject.getAttributes(), hasItem(new Attribute("upd-to", "noreply@ripe.net")));
        assertThat(values(whoisObject, "admin-c"), hasItem("TP1-TEST"));
        assertThat(attribute(whoisObject).getComment(), is(not("Filtered")));
    }

    @Test
    public void allow_listed_oidc_session_gets_unfiltered_version() {
        final String response = SecureRestTest.target(getSecurePort(), MNTNER_VERSION_PATH)
                .request(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenForOidc(BASIC_AUTH_PERSON_ANY_MNT))
                .get(String.class);

        assertThat(response, containsString(AUTH_HASH));
        assertThat(response, containsString("upd-to:"));
        assertThat(response, containsString("admin-c:"));
        assertThat(response, not(containsString("# Filtered")));
    }

    @Test
    public void oidc_session_not_on_allow_list_gets_filtered_version() {
        final String response = SecureRestTest.target(getSecurePort(), MNTNER_VERSION_PATH)
                .request(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenForOidc(BASIC_AUTH_TEST_NO_MNT))
                .get(String.class);

        assertThat(response, containsString("MD5-PW # Filtered"));
        assertThat(response, not(containsString(AUTH_HASH)));
    }

    @Test
    public void expired_oidc_token_gets_filtered_version() {
        final String response = SecureRestTest.target(getSecurePort(), MNTNER_VERSION_PATH)
                .request(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenForOidc(BASIC_AUTH_EXPIRED))
                .get(String.class);

        assertThat(response, containsString("MD5-PW # Filtered"));
    }

    @Test
    public void no_credentials_gets_filtered_version() {
        final String response = SecureRestTest.target(getSecurePort(), MNTNER_VERSION_PATH)
                .request(MediaType.TEXT_PLAIN)
                .get(String.class);

        assertThat(response, containsString("MD5-PW # Filtered"));
    }

    @Test
    public void crowd_cookie_takes_precedence_over_oidc_session() {
        final String response = SecureRestTest.target(getSecurePort(), MNTNER_VERSION_PATH)
                .request(MediaType.TEXT_PLAIN)
                .cookie("crowd.token_key", "invalid-token")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenForOidc(BASIC_AUTH_PERSON_ANY_MNT))
                .get(String.class);

        assertThat(response, containsString("MD5-PW # Filtered"));
    }

    @Test
    public void allow_listed_crowd_cookie_still_works_over_https() {
        final String response = SecureRestTest.target(getSecurePort(), MNTNER_VERSION_PATH)
                .request(MediaType.TEXT_PLAIN)
                .cookie("crowd.token_key", "valid-token")
                .get(String.class);

        assertThat(response, containsString(AUTH_HASH));
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

    private static Attribute attribute(final WhoisObject whoisObject) {
        return whoisObject.getAttributes().stream()
                .filter(attribute -> attribute.getName().equals("source"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no attribute " + "source"));
    }

    private static List<String> values(final WhoisObject whoisObject, final String name) {
        return whoisObject.getAttributes().stream()
                .filter(attribute -> attribute.getName().equals(name))
                .map(Attribute::getValue)
                .toList();
    }
}
