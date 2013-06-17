package net.ripe.db.whois.query.integration;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.transform.FilterAuthFunction;
import net.ripe.db.whois.common.rpsl.transform.FilterEmailFunction;
import net.ripe.db.whois.common.support.DummyWhoisClient;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.query.domain.QueryMessages;
import net.ripe.db.whois.query.support.AbstractWhoisIntegrationTest;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.loadScripts;
import static net.ripe.db.whois.query.integration.VersionTestIntegration.VersionMatcher.containsFilteredVersion;
import static net.ripe.db.whois.query.support.PatternMatcher.matchesPattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@Category(IntegrationTest.class)
public class VersionTestIntegration extends AbstractWhoisIntegrationTest {

    public static class VersionMatcher extends BaseMatcher<String> {
        private final String expected;

        public VersionMatcher(String expected) {
            this.expected = expected;
        }

        @Override
        public boolean matches(Object item) {
            return item instanceof String && ((String) item).contains(expected);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Version object: ").appendValue(expected);
        }

        @Factory
        public static Matcher<String> containsUnfilteredVersion(RpslObject object) {
            return new VersionMatcher(makeMatcherString(object, false));
        }

        @Factory
        public static Matcher<String> containsFilteredVersion(RpslObject object) {
            return new VersionMatcher(makeMatcherString(object, true));
        }

        private static String makeMatcherString(RpslObject object, boolean filtered) {
            StringBuilder expecting = new StringBuilder();

            FilterAuthFunction authFilter = new FilterAuthFunction();
            RpslObject filteredObject = authFilter.apply(object);

            if (filtered) {
                FilterEmailFunction emailFilter = new FilterEmailFunction();
                filteredObject = emailFilter.apply(filteredObject);
            }

            expecting.append(filteredObject != null ? filteredObject.toString() : null);

            return expecting.toString();
        }
    }

    @Before
    public void startup() {
        loadScripts(databaseHelper.getWhoisTemplate(), "broken.sql");

        databaseHelper.addObject(RpslObject.parse("person: Test User\nnic-hdl: TU1-TEST\nsource: TEST"));

        queryServer.start();
    }

    @After
    public void teardown() {
        queryServer.stop();
    }

    @Test
    public void noObject() {
        final String response = stripHeader(DummyWhoisClient.query(QueryServer.port, "--list-versions AS-FOO"));
        assertThat(response, containsString(QueryMessages.noResults("TEST").toString()));
    }

    @Test
    public void noHistory() {
        final String response = stripHeader(DummyWhoisClient.query(QueryServer.port, "--list-versions AS-TEST"));
        assertThat(response, matchesPattern("1\\s+2002-09-1[78] \\d\\d:\\d\\d\\s+ADD/UPD"));
    }

    @Test
    public void noHistoryOnDeletedObject() {
        final String response = stripHeader(DummyWhoisClient.query(QueryServer.port, "--list-versions test.sk"));
        assertThat(response, matchesPattern("This object was deleted on 2003-08-1[23] \\d\\d:\\d\\d"));
    }

    @Test
    public void simpleHistory() {
        final String response = stripHeader(DummyWhoisClient.query(QueryServer.port, "--list-versions AS20507"));
        assertThat(response, matchesPattern("1\\s+2002-09-1[78] \\d\\d:\\d\\d\\s+ADD/UPD"));
        assertThat(response, matchesPattern("2\\s+2002-09-2[34] \\d\\d:\\d\\d\\s+ADD/UPD"));
        assertThat(response, matchesPattern("4\\s+2002-10-1[56] \\d\\d:\\d\\d\\s+ADD/UPD\n\n"));
    }

    @Test
    public void longHistory() {
        final String response = stripHeader(DummyWhoisClient.query(QueryServer.port, "--list-versions AS20507"));

        String[] lines = new String[]{
                "1\\s+2002-09-1[78] \\d\\d:\\d\\d\\s+ADD/UPD",
                "2\\s+2002-09-2[34] \\d\\d:\\d\\d\\s+ADD/UPD",
                "3\\s+2002-10-1[45] \\d\\d:\\d\\d\\s+ADD/UPD",
                "4\\s+2002-10-1[56] \\d\\d:\\d\\d\\s+ADD/UPD"
        };

        for (String check : lines) {
            assertThat(response, matchesPattern(check));
        }
    }

    @Test
    public void personTest() {
        final String response = stripHeader(DummyWhoisClient.query(QueryServer.port, "--list-versions TU1-TEST"));
        assertThat(response, containsString(QueryMessages.versionPersonRole("PERSON", "TU1-TEST").toString()));
    }

    @Test
    public void roleTest() {
        databaseHelper.addObject(RpslObject.parse("role: Some User\nnic-hdl: SU1-TEST\nsource: TEST"));

        final String response = stripHeader(DummyWhoisClient.query(QueryServer.port, "--list-versions SU1-TEST"));
        assertThat(response, containsString(QueryMessages.versionPersonRole("ROLE", "SU1-TEST").toString()));
    }

    @Test
    public void getObjects() {
        String response = stripHeader(DummyWhoisClient.query(QueryServer.port, "--show-version 1 AS20507"));
        assertThat(response, containsFilteredVersion(
                RpslObject.parse("" +
                        "aut-num: AS20507\n" +
                        "as-name: AsName\n" +
                        "descr:   sequence 81\n" +
                        "source:  RIPE")));


        response = stripHeader(DummyWhoisClient.query(QueryServer.port, "--show-version 2 AS20507"));
        assertThat(response, containsFilteredVersion(
                RpslObject.parse("" +
                        "aut-num: AS20507\n" +
                        "as-name: AsName\n" +
                        "descr:   sequence 82\n" +
                        "source:  RIPE")));

        response = stripHeader(DummyWhoisClient.query(QueryServer.port, "--show-version 3 AS20507"));
        assertThat(response, containsFilteredVersion(
                RpslObject.parse("" +
                        "aut-num: AS20507\n" +
                        "as-name: AsName\n" +
                        "descr:   sequence 83\n" +
                        "source:  RIPE")));
        assertThat(response, not(containsString("(current version)")));

        response = stripHeader(DummyWhoisClient.query(QueryServer.port, "--show-version 4 AS20507"));
        assertThat(response, containsFilteredVersion(
                RpslObject.parse("" +
                        "aut-num: AS20507\n" +
                        "as-name: AsName\n" +
                        "descr: sequence 84\n" +
                        "source:  RIPE")));
        assertThat(response, containsString("(current version)"));

        response = stripHeader(DummyWhoisClient.query(QueryServer.port, "--show-version 140 AS20507"));
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

        String response = stripHeader(DummyWhoisClient.query(QueryServer.port, "--show-version 1 MAINT-ME"));
        assertThat(response, containsFilteredVersion(object));

        response = stripHeader(DummyWhoisClient.query(QueryServer.port, "--show-version 1 -B MAINT-ME"));
        assertThat(response, containsString("%ERROR:109: invalid combination of flags passed"));
        assertThat(response, containsString("% The flags \"--show-version\" and \"-B, --no-filtering\" cannot be used together."));
    }

    @Test
    public void listVersions_to_suppress_filtered_warning() {
        final String result = DummyWhoisClient.query(QueryServer.port, "--list-versions AS-TEST");
        assertThat(result, not(containsString("Note: this output has been filtered")));
    }

    @Test
    public void showVersion_bizarreVersionNumber() {
        final String response = stripHeader(DummyWhoisClient.query(QueryServer.port, "--show-version 1.5 MAINT-ME"));

        assertThat(response, not(containsString("Only one version flag (--version) allowed")));
    }

    @Test
    public void showVersion_lastVersionDeleted() {
        databaseHelper.addObject(RpslObject.parse("mntner: TEST-DBM"));
        databaseHelper.removeObject(RpslObject.parse("mntner: TEST-DBM"));

        final String response = stripHeader(DummyWhoisClient.query(QueryServer.port, "--show-version 1 TEST-DBM"));

        assertThat(response, containsString("This object was deleted on"));
    }

    @Test
    public void listVersion_inetnum_without_space_in_range() throws Exception {
        databaseHelper.addObject("" +
                "inetnum: 192.168.0.0 - 192.168.255.255\n" +
                "netname: TEST");

        final String response = stripHeader(DummyWhoisClient.query(QueryServer.port, "--list-versions 192.168.0.0-192.168.255.255"));
        assertThat(response, containsString("Version history for INETNUM object \"192.168.0.0 - 192.168.255.255\""));
        assertThat(response, containsString("ADD/UPD"));
    }

    @Test
    public void listVersions_undeleted() {
        final RpslObjectUpdateInfo createdObjectInfo = rpslObjectUpdateDao.createObject(RpslObject.parse("mntner: TEST-DBM"));
        rpslObjectUpdateDao.deleteObject(createdObjectInfo.getObjectId(), createdObjectInfo.getKey());
        rpslObjectUpdateDao.undeleteObject(createdObjectInfo.getObjectId());

        final String response = stripHeader(DummyWhoisClient.query(QueryServer.port, "--list-versions TEST-DBM"));
        assertThat(response, containsString("This object was deleted on"));
        assertThat(response, containsString("ADD/UPD"));
    }

    @Test
    public void showVersion_undeleted() {
        final RpslObjectUpdateInfo createdObjectInfo = rpslObjectUpdateDao.createObject(RpslObject.parse("mntner: TEST-DBM"));
        rpslObjectUpdateDao.deleteObject(createdObjectInfo.getObjectId(), createdObjectInfo.getKey());
        rpslObjectUpdateDao.undeleteObject(createdObjectInfo.getObjectId());

        final String response = stripHeader(DummyWhoisClient.query(QueryServer.port, "--show-version 1 TEST-DBM"));
        assertThat(response, not(containsString("This object was deleted on")));
        assertThat(response, containsString("mntner:         TEST-DBM"));
    }
}
