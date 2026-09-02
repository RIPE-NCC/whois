package net.ripe.db.whois.api.rest;

import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.domain.WhoisVersion;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class WhoisVersionFullHistoryTestIntegration extends AbstractIntegrationTest {
    private static final String VERSIONS_PATH = "whois/test/aut-num/AS102/versions";

    private static final RpslObject AUTNUM_V2 = RpslObject.parse(
            """
            aut-num:        AS102
            as-name:        End-User-2
            descr:          description
            admin-c:        TP1-TEST
            tech-c:         TP1-TEST
            mnt-by:         OWNER-MNT
            source:         TEST
            """);

    private static final RpslObject AUTNUM_V3 = RpslObject.parse(
            """
            aut-num:        AS102
            as-name:        End-User-3
            descr:          description
            admin-c:        TP1-TEST
            tech-c:         TP1-TEST
            mnt-by:         OWNER-MNT
            source:         TEST
            """);

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
                auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test
                mnt-by:      OWNER-MNT
                source:      TEST
                """);
    }

    private void createDeleteRecreateHistory() {
        databaseHelper.addObject(AUTNUM_V2);
        databaseHelper.deleteObject(AUTNUM_V2);
        databaseHelper.addObject(AUTNUM_V2);
        databaseHelper.updateObject(AUTNUM_V3);
        databaseHelper.deleteObject(AUTNUM_V3);
        databaseHelper.addObject(AUTNUM_V2);
        databaseHelper.updateObject(AUTNUM_V2);
    }

    @Test
    public void internal_user_sees_full_history_across_deletions() {
        createDeleteRecreateHistory();

        final List<WhoisVersion> versions = internal(VERSIONS_PATH).get(WhoisResources.class).getVersions().getVersions();

        assertThat(versions, hasSize(7));
        assertThat(operations(versions), contains("ADD/UPD", "DEL", "ADD/UPD", "ADD/UPD", "DEL", "ADD/UPD", "ADD/UPD"));
        assertThat(revisions(versions), contains(1, 2, 3, 4, 5, 6, 7));
        for (final WhoisVersion version : versions) {
            assertThat(version.getDeletedDate(), is(nullValue()));
        }
    }

    private Invocation.Builder internal(final String path) {
        return internal(path, MediaType.APPLICATION_XML);
    }

    private Invocation.Builder internal(final String path, final String mediaType) {
        return RestTest.target(getPort(), path)
                .request(mediaType)
                .cookie("crowd.token_key", "valid-token");
    }

    private static List<String> operations(final List<WhoisVersion> versions) {
        return versions.stream().map(WhoisVersion::getOperation).toList();
    }

    private static List<Integer> revisions(final List<WhoisVersion> versions) {
        return versions.stream().map(WhoisVersion::getRevision).toList();
    }

}
