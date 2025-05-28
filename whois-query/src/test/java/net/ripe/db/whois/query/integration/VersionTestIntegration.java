package net.ripe.db.whois.query.integration;

import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.dao.VersionDateTime;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.transform.FilterAuthFunction;
import net.ripe.db.whois.common.rpsl.transform.FilterEmailFunction;
import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.query.support.AbstractQueryIntegrationTest;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.loadScripts;
import static net.ripe.db.whois.query.integration.VersionTestIntegration.VersionMatcher.containsFilteredVersion;
import static net.ripe.db.whois.query.support.PatternMatcher.matchesPattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@Tag("IntegrationTest")
public class VersionTestIntegration extends AbstractQueryIntegrationTest {

    @BeforeEach
    public void startup() {
        loadScripts(databaseHelper.getWhoisTemplate(), "broken.sql");

        databaseHelper.addObject(RpslObject.parse("person: Test User\nnic-hdl: TU1-TEST\nsource: TEST"));

        queryServer.start();
    }

    @AfterEach
    public void teardown() {
        queryServer.stop(true);
    }

    protected String historyTimestampToString(long timestamp) {
        return new VersionDateTime(timestamp).toString();
    }

    @Test
    public void noObject() {
        final String response = stripHeader(TelnetWhoisClient.queryLocalhost(queryServer.getPort(), "--list-versions AS-FOO"));
        assertThat(response, containsString(QueryMessages.noResults("TEST").toString()));
    }

    @Test
    public void noHistory() {
        final String response = stripHeader(TelnetWhoisClient.queryLocalhost(queryServer.getPort(), "--list-versions AS-TEST"));
        assertThat(response, matchesPattern("1\\s+" + historyTimestampToString(1032338056) + "\\s+ADD/UPD"));
    }

    @Test
    public void noHistoryOnDeletedObject() {
        final String response = stripHeader(TelnetWhoisClient.queryLocalhost(queryServer.getPort(), "--list-versions test.sk"));
        assertThat(response, matchesPattern("This object was deleted on " + historyTimestampToString(1060699626)));
    }

    @Test
    public void simpleHistory() {
        final String response = stripHeader(TelnetWhoisClient.queryLocalhost(queryServer.getPort(), "--list-versions AS20507"));
        assertThat(response, matchesPattern("1\\s+" + historyTimestampToString(1032341936) + "\\s+ADD/UPD"));
        assertThat(response, matchesPattern("2\\s+" + historyTimestampToString(1032857323) + "\\s+ADD/UPD"));
        assertThat(response, matchesPattern("4\\s+" + historyTimestampToString(1034685022) + "\\s+ADD/UPD\n\n"));
    }

    @Test
    public void longHistory() {
        final String response = stripHeader(TelnetWhoisClient.queryLocalhost(queryServer.getPort(), "--list-versions AS20507"));

        String[] lines = new String[]{
                "1\\s+" + historyTimestampToString(1032341936) + "\\s+ADD/UPD",
                "2\\s+" + historyTimestampToString(1032857323) + "\\s+ADD/UPD",
                "3\\s+" + historyTimestampToString(1034602217) + "\\s+ADD/UPD",
                "4\\s+" + historyTimestampToString(1034685022) + "\\s+ADD/UPD"
        };

        for (String check : lines) {
            assertThat(response, matchesPattern(check));
        }
    }

    @Test
    public void personTest() {
        final String response = stripHeader(TelnetWhoisClient.queryLocalhost(queryServer.getPort(), "--list-versions TU1-TEST"));
        assertThat(response, containsString(QueryMessages.versionPersonRole("PERSON", "TU1-TEST").toString()));
    }

    @Test
    public void roleTest() {
        databaseHelper.addObject(RpslObject.parse("role: Some User\nnic-hdl: SU1-TEST\nsource: TEST"));

        final String response = stripHeader(TelnetWhoisClient.queryLocalhost(queryServer.getPort(), "--list-versions SU1-TEST"));
        assertThat(response, containsString(QueryMessages.versionPersonRole("ROLE", "SU1-TEST").toString()));
    }

    @Test
    public void getObjects() {
        String response = stripHeader(TelnetWhoisClient.queryLocalhost(queryServer.getPort(), "--show-version 1 AS20507"));
        assertThat(response, containsFilteredVersion(
                RpslObject.parse("" +
                        "aut-num: AS20507\n" +
                        "as-name: AsName\n" +
                        "descr:   sequence 81\n" +
                        "source:  RIPE")));


        response = stripHeader(TelnetWhoisClient.queryLocalhost(queryServer.getPort(), "--show-version 2 AS20507"));
        assertThat(response, containsFilteredVersion(
                RpslObject.parse("" +
                        "aut-num: AS20507\n" +
                        "as-name: AsName\n" +
                        "descr:   sequence 82\n" +
                        "source:  RIPE")));

        response = stripHeader(TelnetWhoisClient.queryLocalhost(queryServer.getPort(), "--show-version 3 AS20507"));
        assertThat(response, containsFilteredVersion(
                RpslObject.parse("" +
                        "aut-num: AS20507\n" +
                        "as-name: AsName\n" +
                        "descr:   sequence 83\n" +
                        "source:  RIPE")));
        assertThat(response, not(containsString("(current version)")));

        response = stripHeader(TelnetWhoisClient.queryLocalhost(queryServer.getPort(), "--show-version 4 AS20507"));
        assertThat(response, containsFilteredVersion(
                RpslObject.parse("" +
                        "aut-num: AS20507\n" +
                        "as-name: AsName\n" +
                        "descr: sequence 84\n" +
                        "source:  RIPE")));
        assertThat(response, containsString("(current version)"));

        response = stripHeader(TelnetWhoisClient.queryLocalhost(queryServer.getPort(), "--show-version 140 AS20507"));
        assertThat(response, containsString(QueryMessages.versionOutOfRange(4).toString()));
    }

    @Test
    public void versionFilteringNotAllowed() {
        RpslObject object = RpslObject.parse("" +
                "mntner:  MAINT-ME\n" +
                "descr:   Testing maintainer\n" +
                "auth:    MD5-PW $1$1ZJHr26K$4whSGLye/Ml92RTm7PQgu/\n" +
                "source:  RIPE");
        databaseHelper.addObject(object);

        String response = stripHeader(TelnetWhoisClient.queryLocalhost(queryServer.getPort(), "--show-version 1 MAINT-ME"));
        System.out.println(response);
        assertThat(response, containsFilteredVersion(object));

        response = stripHeader(TelnetWhoisClient.queryLocalhost(queryServer.getPort(), "--show-version 1 -B MAINT-ME"));
        assertThat(response, containsString("%ERROR:109: invalid combination of flags passed"));
        assertThat(response, containsString("% The flags \"--show-version\" and \"-B, --no-filtering\" cannot be used together."));
    }

    @Test
    public void listVersions_to_suppress_filtered_warning() {
        final String result = TelnetWhoisClient.queryLocalhost(queryServer.getPort(), "--list-versions AS-TEST");
        assertThat(result, not(containsString("Note: this output has been filtered")));
    }

    @Test
    public void showVersion_bizarreVersionNumber() {
        final String response = stripHeader(TelnetWhoisClient.queryLocalhost(queryServer.getPort(), "--show-version 1.5 MAINT-ME"));

        assertThat(response, not(containsString("Only one version flag (--version) allowed")));
    }

    @Test
    public void showVersion_lastVersionDeleted() {
        databaseHelper.addObject(RpslObject.parse("mntner: TEST-DBM"));
        databaseHelper.deleteObject(RpslObject.parse("mntner: TEST-DBM"));

        final String response = stripHeader(TelnetWhoisClient.queryLocalhost(queryServer.getPort(), "--show-version 1 TEST-DBM"));

        assertThat(response, containsString("This object was deleted on"));
    }

    @Test
    public void listVersion_inetnum_without_space_in_range() throws Exception {
        databaseHelper.addObject("" +
                "inetnum: 192.168.0.0 - 192.168.255.255\n" +
                "netname: TEST");

        final String response = stripHeader(TelnetWhoisClient.queryLocalhost(queryServer.getPort(), "--list-versions 192.168.0.0-192.168.255.255"));
        assertThat(response, containsString("Version history for INETNUM object \"192.168.0.0 - 192.168.255.255\""));
        assertThat(response, containsString("ADD/UPD"));
    }

    @Test
    public void listVersions_undeleted() {
        final RpslObjectUpdateInfo createdObjectInfo = rpslObjectUpdateDao.createObject(RpslObject.parse("mntner: TEST-DBM"));
        rpslObjectUpdateDao.deleteObject(createdObjectInfo.getObjectId(), createdObjectInfo.getKey());
        rpslObjectUpdateDao.undeleteObject(createdObjectInfo.getObjectId());

        final String response = stripHeader(TelnetWhoisClient.queryLocalhost(queryServer.getPort(), "--list-versions TEST-DBM"));
        assertThat(response, containsString("This object was deleted on"));
        assertThat(response, containsString("ADD/UPD"));
    }

    @Test
    public void showVersion_undeleted() {
        final RpslObjectUpdateInfo createdObjectInfo = rpslObjectUpdateDao.createObject(RpslObject.parse("mntner: TEST-DBM"));
        rpslObjectUpdateDao.deleteObject(createdObjectInfo.getObjectId(), createdObjectInfo.getKey());
        rpslObjectUpdateDao.undeleteObject(createdObjectInfo.getObjectId());

        final String response = stripHeader(TelnetWhoisClient.queryLocalhost(queryServer.getPort(), "--show-version 1 TEST-DBM"));
        assertThat(response, not(containsString("This object was deleted on")));
        assertThat(response, containsString("mntner:         TEST-DBM"));
    }

    @Test
    public void showVersion_timestamps() {
        databaseHelper.addObject("" +
                "organisation: TO1-TEST\n" +
                "org-name: Test Organisation\n" +
                "created: 2015-02-02T11:12:13Z\n" +
                "last-modified: 2015-02-02T11:12:13Z\n" +
                "source: TEST");

        final String response = stripHeader(TelnetWhoisClient.queryLocalhost(queryServer.getPort(), "--show-version 1 TO1-TEST"));
        assertThat(response, containsString("created:        2015-02-02T11:12:13Z"));
        assertThat(response, containsString("last-modified:  2015-02-02T11:12:13Z"));
    }

    public static class VersionMatcher extends BaseMatcher<String> {
        private final String expected;

        public VersionMatcher(final String expected) {
            this.expected = expected;
        }

        @Override
        public boolean matches(final Object item) {
            return item instanceof String && ((String) item).contains(expected);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Version object: ").appendValue(expected);
        }

        public static Matcher<String> containsFilteredVersion(final RpslObject object) {
            return new VersionMatcher(makeMatcherString(object, true));
        }

        private static String makeMatcherString(final RpslObject object, final boolean filtered) {
            final StringBuilder expecting = new StringBuilder();
            final FilterAuthFunction authFilter = new FilterAuthFunction();
            RpslObject filteredObject = authFilter.apply(object);

            if (filtered) {
                filteredObject = new FilterEmailFunction().apply(filteredObject);
            }

            expecting.append(filteredObject != null ? filteredObject.toString() : null);

            return expecting.toString();
        }
    }
}
