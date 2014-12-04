package net.ripe.db.whois.query.integration;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.TestDateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.iptree.IpTreeUpdater;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.common.support.NettyWhoisClientFactory;
import net.ripe.db.whois.common.support.WhoisClientHandler;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.query.support.AbstractQueryIntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Set;

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.insertIntoLastAndUpdateSerials;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.insertIntoTablesIgnoreMissing;
import static net.ripe.db.whois.common.support.StringMatchesRegexp.stringMatchesRegexp;
import static net.ripe.db.whois.query.support.PatternCountMatcher.matchesPatternCount;
import static net.ripe.db.whois.query.support.PatternMatcher.matchesPattern;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class SimpleTestIntegration extends AbstractQueryIntegrationTest {
    //TODO [TP]: Too many different things being tested here. Should be refactored.

    private static final String END_OF_HEADER = "% See http://www.ripe.net/db/support/db-terms-conditions.pdf\n\n";

    @Autowired IpTreeUpdater ipTreeUpdater;
    @Autowired TestDateTimeProvider dateTimeProvider;

    // TODO: [AH] most tests don't taint the DB; have a 'tainted' flag in DBHelper, reinit only if needed
    @Before
    public void startupWhoisServer() {
        final RpslObject person = RpslObject.parse("person: ADM-TEST\naddress: address\nphone: +312342343\nmnt-by:RIPE-NCC-HM-MNT\nadmin-c: ADM-TEST\nchanged: dbtest@ripe.net 20120707\nnic-hdl: ADM-TEST");
        final RpslObject mntner = RpslObject.parse("mntner: RIPE-NCC-HM-MNT\nmnt-by: RIPE-NCC-HM-MNT\ndescr: description\nadmin-c: ADM-TEST");
        databaseHelper.addObjects(Lists.newArrayList(person, mntner));

        databaseHelper.addObject("inetnum: 81.0.0.0 - 82.255.255.255\nnetname: NE\nmnt-by:RIPE-NCC-HM-MNT");
        databaseHelper.addObject("domain: 117.80.81.in-addr.arpa");
        databaseHelper.addObject("inetnum: 81.80.117.237 - 81.80.117.237\nnetname: NN\nstatus: OTHER");
        databaseHelper.addObject("route: 81.80.117.0/24\norigin: AS123\n");
        databaseHelper.addObject("route: 81.80.0.0/16\norigin: AS123\n");
        ipTreeUpdater.rebuild();
        queryServer.start();
    }

    @After
    public void shutdownWhoisServer() {
        queryServer.stop(true);
    }

    @Test
    public void testLoggingNonProxy() {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-rBGxTinetnum 81.80.117.237 - 81.80.117.237");
        assertThat(response, containsString("81.80.117.237 - 81.80.117.237"));
    }

    @Test
    public void testLocalhostAllowedToProxyRequest() {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-V 10.0.0.1 -rBGxTinetnum 81.80.117.237 - 81.80.117.237");

        assertThat(response, containsString("81.80.117.237 - 81.80.117.237"));
    }

    @Test
    public void testLoggingProxy() throws InterruptedException {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "help\n");

        assertThat(response, containsString("-L"));
    }

    @Test
    public void kFlagShouldKeepTheConnectionOpenUntilTheSecondKWithoutArguments() throws Exception {
        final WhoisClientHandler client = NettyWhoisClientFactory.newLocalClient(QueryServer.port);

        client.connectAndWait();
        client.sendLine("-k");

        client.waitForResponseEndsWith(END_OF_HEADER);

        client.sendLine("-k");
        client.waitForClose();

        assertTrue(client.getSuccess());
    }

    @Test
    public void kFlagShouldKeepTheConnectionOpenAfterSupportedQuery() throws Exception {
        final WhoisClientHandler client = NettyWhoisClientFactory.newLocalClient(QueryServer.port);

        client.connectAndWait();
        client.sendLine("-k");

        client.waitForResponseEndsWith(END_OF_HEADER);
        client.clearBuffer();

        client.sendLine("-rBGxTinetnum 81.80.117.237 - 81.80.117.237");
        client.waitForResponseEndsWith(END_OF_HEADER);

        assertThat(client.getResponse(), containsString("inetnum:        81.80.117.237 - 81.80.117.237"));

        client.sendLine("-k");
        client.waitForClose();

        assertTrue(client.getSuccess());
    }

    @Test
    public void testEmptyQueries() throws Exception {
        for (int i = 0; i < 10; i++) {
            final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "\n");

            assertThat(response, containsString(QueryMessages.noSearchKeySpecified().toString()));
        }
    }

    @Test
    public void testEmptyQueriesOnMultipleLines() throws Exception {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "\n\n\n");

        assertThat(response, containsString(QueryMessages.noSearchKeySpecified().toString()));
        assertThat(response, not(containsString(QueryMessages.internalErroroccurred().toString())));
    }

    @Test
    public void multiple_lines_unsupported() throws Exception {
        TelnetWhoisClient.queryLocalhost(QueryServer.port, "-rwhois V-1.5 jwhois 1.0\n-rwhois V-1.5 jwhois 2.0\n-rwhois V-1.5 jwhois 3.0\n");
    }

    @Test
    public void testMultipleQueriesWithoutKeepAlive() throws Exception {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "help\nhelp");

        assertThat(response, containsString("RIPE Database Reference Manual"));
        assertThat(response, not(containsString(QueryMessages.internalErroroccurred().toString())));
    }

    @Test
    public void testGetVersion() throws Exception {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-q version");

        assertThat(response, containsString("% whois-server-"));
        assertThat(response, not(containsString(QueryMessages.internalErroroccurred().toString())));
    }

    @Test
    public void testGetVersion_with_long_option() throws Exception {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "--version");

        assertThat(response, containsString("% whois-server-"));
        assertThat(response, not(containsString(QueryMessages.internalErroroccurred().toString())));
    }

    @Test
    public void testGetInvalidInetnum() throws Exception {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-r -T inetnum RIPE-MNT");

        assertThat(response, containsString("%ERROR:101: no entries found"));
        assertThat(response, not(containsString(QueryMessages.internalErroroccurred().toString())));
    }

    @Test
    public void testGetMaintainer() throws Exception {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-r -T mntner RIPE-MNT");

        assertThat(response, containsString("%ERROR:101: no entries found"));
        assertThat(response, not(containsString(QueryMessages.internalErroroccurred().toString())));
    }

    @Test
    public void testGetMaintainer_long_version() throws Exception {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "--no-referenced --select-types mntner RIPE-MNT");

        assertThat(response, containsString("%ERROR:101: no entries found"));
        assertThat(response, not(containsString(QueryMessages.internalErroroccurred().toString())));
    }

    @Test
    public void searchByOrganisationNameNoSearchKey() throws Exception {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-r -T organisation");

        assertThat(response, containsString("no search key specified"));
        assertThat(response, not(containsString(QueryMessages.internalErroroccurred().toString())));
    }

    @Test
    public void searchByAsBlockInvalidRange() throws Exception {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-r -T as-block AS2 - AS1");

        assertThat(response, containsString(QueryMessages.invalidSearchKey().toString()));
        assertThat(response, not(containsString(QueryMessages.internalErroroccurred().toString())));
    }

    @Test
    public void domainQueryTrailingDot() throws Exception {
        databaseHelper.addObject("domain: 9.4.e164.arpa");
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-rT domain 9.4.e164.arpa.");

        assertThat(response, not(containsString("trailing dot in domain query")));
        assertThat(response, not(containsString(QueryMessages.internalErroroccurred().toString())));
    }

    @Test
    public void reverseDomainInvalidInverseRange() throws Exception {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "80-28.79.198.195.in-addr.arpa");

        assertThat(response, containsString("no entries found"));
        assertThat(response, not(containsString(QueryMessages.internalErroroccurred().toString())));
    }

    @Test
    public void reverseDomainUppercaseSearch() throws Exception {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "117.80.81.IN-ADDR.ARPA");

        assertThat(response, containsString("117.80.81.in-addr.arpa"));
        assertThat(response, not(containsString(QueryMessages.noResults("TEST").toString())));
    }

    @Test
    public void routeSimpleHierarchySearch() throws Exception {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "81.80.117.0/24AS123");
        assertThat(response, matchesPattern("(?m)^route: *81.80.117.0/24$"));
        assertThat(response, matchesPatternCount("(?m)^\\w+: *", 2));
    }

    @Test
    public void routeSimpleHierarchySearchWrongAS() throws Exception {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "81.80.117.0/24AS456");
        assertThat(response, containsString(QueryMessages.noResults("TEST").toString()));
    }

    @Test
    public void routeDefaultHierarchySearchForNonexistant() throws Exception {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "81.80.117.54/32AS123");
        assertThat(response, containsString(QueryMessages.noResults("TEST").toString()));
    }

    @Test
    public void routeAllLessSpecificHierarchySearchForNonexistant() throws Exception {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-L 81.80.117.54/32AS123");
        assertThat(response, matchesPattern("(?m)^route: *81.80.117.0/24$"));
        assertThat(response, matchesPattern("(?m)^route: *81.80.0.0/16$"));
        assertThat(response, matchesPatternCount("(?m)^\\w+: *", 4));
    }

    @Test
    public void routeOneLessSpecificHierarchySearchForExisting() throws Exception {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-l 81.80.117.0/24AS123");
        assertThat(response, matchesPattern("(?m)^route: *81.80.0.0/16$"));
        assertThat(response, matchesPatternCount("(?m)^\\w+: *", 2));
    }

    @Test
    public void routeOneLessSpecificHierarchySearchAtTopLevel() throws Exception {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-l 81.80.0.0/16AS123");
        assertThat(response, containsString(QueryMessages.noResults("TEST").toString()));
    }

    @Test
    public void routeOneMoreSpecificHierarchySearchAtBottomLevel() throws Exception {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-m 81.80.117.0/24AS123");
        assertThat(response, containsString(QueryMessages.noResults("TEST").toString()));
    }

    @Test
    public void routeOneMoreSpecificHierarchySearch() throws Exception {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-m 81.80.0.0/16AS123");
        assertThat(response, matchesPattern("(?m)^route: *81.80.117.0/24$"));
        assertThat(response, matchesPatternCount("(?m)^\\w+: *", 2));
    }

    @Test
    public void routeOneMoreSpecificHierarchySearchAtTopLevel() throws Exception {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-m 0.0.0.0/0AS123");
        assertThat(response, containsString("query options are not allowed on very large ranges/prefixes"));
        assertThat(response, matchesPatternCount("(?m)^\\w+: *", 0));
    }

    @Test
    public void routeOneMoreSpecificHierarchySearchAtAlmostTopLevel() throws Exception {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-m 81.0.0.0/8AS123");
        assertThat(response, matchesPattern("(?m)^route: *81.80.0.0/16$"));
        assertThat(response, matchesPatternCount("(?m)^\\w+: *", 2));
    }

    @Test
    public void routeAllMoreSpecificHierarchySearchAtAlmostTopLevel() throws Exception {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-M 81.0.0.0/8AS123");
        assertThat(response, matchesPattern("(?m)^route: *81.80.0.0/16$"));
        assertThat(response, matchesPattern("(?m)^route: *81.80.117.0/24$"));
        assertThat(response, matchesPatternCount("(?m)^\\w+: *", 4));
    }

    @Test
    public void routeAllMoreSpecificHierarchySearchAtTopLevelWrongAS() throws Exception {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-M 81.0.0.0/8A456");
        assertThat(response, containsString(QueryMessages.noResults("TEST").toString()));
    }

    @Test
    public void personQueryWithoutSearchKey() throws Exception {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-rT person");

        assertThat(response, containsString(QueryMessages.noSearchKeySpecified().toString()));
        assertThat(response, not(containsString(QueryMessages.internalErroroccurred().toString())));
    }

    @Test
    public void invalidAsBlockReturnsErrorMessage() throws Exception {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-rT as-block AS2-AS1");

        assertThat(response, containsString(QueryMessages.invalidSearchKey().toString()));
        assertThat(response, not(containsString(QueryMessages.internalErroroccurred().toString())));
        assertThat(response, not(containsString(QueryMessages.outputFilterNotice().toString())));
    }

    @Test
    public void findByEmailAttributeShouldNotReturnDuplicates() throws Exception {
        databaseHelper.addObject(RpslObject.parse(
                "person:Denis Walker\n" +
                        "e-mail:denis@ripe.net\n" +
                        "nic-hdl:DW-RIPE"));


        databaseHelper.addObject(RpslObject.parse(
                "person:Denis Walker\n" +
                        "e-mail:denis@ripe.net\n" +
                        "nic-hdl:DW6465-RIPE"));

        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "denis@ripe.net");

        assertThat(response.indexOf("nic-hdl:        DW-RIPE"), not(is(-1)));
        assertThat(response.lastIndexOf("nic-hdl:        DW-RIPE"), is(response.indexOf("nic-hdl:        DW-RIPE")));

        assertThat(response.indexOf("nic-hdl:        DW6465-RIPE"), not(is(-1)));
        assertThat(response.lastIndexOf("nic-hdl:        DW6465-RIPE"), is(response.indexOf("nic-hdl:        DW6465-RIPE")));
    }

    @Test
    public void unsupported_query() {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "(85.115.248.176)");
        assertThat(response, containsString(QueryMessages.invalidSearchKey().toString()));
    }

    @Test
    public void more_specific_inetnum_query_including_domain_object() {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-r -d -M 81.80.0.0/16");
        assertThat(response, containsString("inetnum:        81.80.117.237 - 81.80.117.237"));
        assertThat(response, containsString("domain:         117.80.81.in-addr.arpa"));
    }

    @Test
    public void check_inverse_with_objecttype() {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-T aut-num -i member-of AS-123");
        assertThat(response, not(containsString(QueryMessages.invalidSearchKey().toString())));
        assertThat(response, containsString(QueryMessages.noResults("TEST").toString()));
    }

    @Test
    public void query_object_missing_references() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "inetnum:      0.0.0.0 - 255.255.255.255\n" +
                "netname:      IANA-BLK\n" +
                "descr:        The whole IPv4 address space\n" +
                "country:      EU # Country is really world wide\n" +
                "org:          ORG-TT1-TEST\n" +
                "admin-c:      AA1-TEST\n" +
                "tech-c:       AA2-TEST\n" +
                "status:       ALLOCATED UNSPECIFIED\n" +
                "remarks:      The country is really worldwide.\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-lower:    TEST-DBM-MNT\n" +
                "mnt-routes:   TEST-DBM-MNT\n" +
                "remarks:      This is an automatically created object.\n" +
                "changed:      bitbucket@ripe.net 20051031\n" +
                "source:       TEST\n");

        final JdbcTemplate jdbcTemplate = databaseHelper.getWhoisTemplate();
        final RpslObjectUpdateInfo rpslObjectInfo = insertIntoLastAndUpdateSerials(dateTimeProvider, jdbcTemplate, rpslObject);
        final Set<CIString> missing = insertIntoTablesIgnoreMissing(jdbcTemplate, rpslObjectInfo, rpslObject);
        assertThat(missing.size(), greaterThan(0));
        ipTreeUpdater.update();

        final String lookupResponse = TelnetWhoisClient.queryLocalhost(QueryServer.port, "0/0");
        assertThat(lookupResponse, containsString("" +
                "inetnum:        0.0.0.0 - 255.255.255.255\n" +
                "netname:        IANA-BLK\n" +
                "descr:          The whole IPv4 address space\n" +
                "country:        EU # Country is really world wide\n" +
                "org:            ORG-TT1-TEST\n" +
                "admin-c:        AA1-TEST\n" +
                "tech-c:         AA2-TEST\n" +
                "status:         ALLOCATED UNSPECIFIED\n" +
                "remarks:        The country is really worldwide.\n" +
                "mnt-by:         RIPE-NCC-HM-MNT\n" +
                "mnt-lower:      TEST-DBM-MNT\n" +
                "mnt-routes:     TEST-DBM-MNT\n" +
                "remarks:        This is an automatically created object.\n" +
                "source:         TEST # Filtered"));

        final String inverseLookupResponse = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-i mnt-by TEST-ROOT-MNT");
        assertThat(inverseLookupResponse, containsString("%ERROR:101: no entries found"));
    }

    @Test
    public void query_grs() {
        databaseHelper.addObject(RpslObject.parse("" +
                "person:         Test person\n" +
                "nic-hdl:        TEST-PN\n" +
                "source:         RIPE"));

        databaseHelper.addObject(RpslObject.parse("" +
                "mntner:         AS760-MNT\n" +
                "descr:          Description\n" +
                "admin-c:        TEST-PN\n" +
                "auth:           MD5-PW $1$2$34567\n" +
                "source:         RIPE"));

        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-s TEST-GRS AS760-MNT");
        assertThat(response, stringMatchesRegexp("(?si)" +
                "% This is the RIPE Database query service.\n" +
                "% The objects are in RPSL format.\n" +
                "%\n" +
                "% The RIPE Database is subject to Terms and Conditions.\n" +
                "% See http://www.ripe.net/db/support/db-terms-conditions.pdf\n" +
                "\n" +
                "% Information related to 'AS760-MNT'\n" +
                "\n" +
                "mntner:         AS760-MNT\n" +
                "descr:          Description\n" +
                "admin-c:        DUMY-RIPE\n" +
                "auth:           MD5-PW # Filtered\n" +
                "source:         TEST-GRS # Filtered\n" +
                "remarks:        \\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\n" +
                "remarks:        \\* THIS OBJECT IS MODIFIED\n" +
                "remarks:        \\* Please note that all data that is generally regarded as personal\n" +
                "remarks:        \\* data has been removed from this object.\n" +
                "remarks:        \\* To view the original object, please query the RIPE Database at:\n" +
                "remarks:        \\* http://www.ripe.net/whois\n" +
                "remarks:        \\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\n" +
                "\n" +
                "% This query was served by the RIPE Database Query Service version 0.1-TEST \\(.*\\)\n" +
                "\n" +
                "\n"));
    }

    @Test
    public void query_updated_domain() throws Exception {
        databaseHelper.updateObject("domain: 117.80.81.in-addr.arpa\nnserver:ns.example.com\nremark:This is the current version\n");

        String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "117.80.81.in-addr.arpa");

        assertThat(response, containsString("domain:         117.80.81.in-addr.arpa"));
        assertThat(response, containsString("This is the current version"));
    }

    @Test
    public void query_domain_versions() throws Exception {
        databaseHelper.updateObject("domain: 117.80.81.in-addr.arpa\nnserver:ns1.example.com\n");
        databaseHelper.updateObject("domain: 117.80.81.in-addr.arpa\nnserver:ns1.example.com\nnserver:ns2.example.com\n");
        databaseHelper.updateObject("domain: 117.80.81.in-addr.arpa\nnserver:ns.example.com\n");
        databaseHelper.updateObject("domain: 117.80.81.in-addr.arpa\nnserver:ns.example.com\nremark:This is the current version\n");

        String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "--list-versions 117.80.81.in-addr.arpa");

        assertThat(response, containsString("% Version history for DOMAIN object \"117.80.81.in-addr.arpa\"\n% You can use \"--show-version rev#\" to get an exact version of the object."));
        assertThat(response, containsString("ADD/UPD"));

        List<RpslObjectVersions.Entry> entries = RpslObjectVersions.parse(response).getVersions();

        assertTrue(entries.size() == 5);

        int i = 1;
        for (RpslObjectVersions.Entry entry : entries) {
            assertEquals(entry.getVersion(), i);
            assertEquals(entry.getOperation(), RpslObjectVersions.Operation.ADD_UPDATE);

            i++;
        }
    }

    @Test
    public void invalid_combination() {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-v role -v person");
        assertThat(response, containsString("ERROR:110: multiple use of flag"));
        assertThat(response, containsString("The flag \"-v\" cannot be used multiple times."));
    }

    @Test
    public void testDirectRouteLookup() {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "81.80.117.0/24AS123");
        assertThat(response, containsString("81.80.117.0/24"));
    }

    @Test
    public void validSyntax_missing_attribute() {
        databaseHelper.addObject("" +
                "mntner:      DEL-MNT\n" +
                "descr:       MNTNER for test\n" +
                "descr:       object not identical to one above\n" +
                "admin-c:     ADM-TEST\n" +
                "upd-to:      dbtest@ripe.net\n" +
                "auth:        MD5-PW $1$T6B4LEdb$5IeIbPNcRJ35P1tNoXFas/  #delete\n" +
                "referral-by: DEL-MNT\n" +
                "changed:     dbtest@ripe.net\n" +
                "source:      TEST");

        final String result = TelnetWhoisClient.queryLocalhost(QueryServer.port, "--valid-syntax DEL-MNT");

        assertThat(result, containsString("% 'DEL-MNT' invalid syntax"));
    }

    @Test
    public void validSyntax_incorrect_attribute() {
        databaseHelper.addObject("" +
                "mntner:      DEL-MNT\n" +
                "descr:       MNTNER for test\n" +
                "descr:       object not identical to one above\n" +
                "admin-c:     ADM-TEST\n" +
                "upd-to:      dbtest_at_ripe.net\n" +
                "auth:        MD5-PW $1$T6B4LEdb$5IeIbPNcRJ35P1tNoXFas/  #delete\n" +
                "referral-by: DEL-MNT\n" +
                "mnt-by:      DEL-MNT\n" +
                "changed:     dbtest@ripe.net\n" +
                "source:      TEST");

        final String result = TelnetWhoisClient.queryLocalhost(QueryServer.port, "--valid-syntax DEL-MNT");

        assertThat(result, containsString("% 'DEL-MNT' invalid syntax"));
    }

    @Test
    public void validSyntax_correct_syntax() {
        databaseHelper.addObject("" +
                "mntner:      DEL-MNT\n" +
                "descr:       MNTNER for test\n" +
                "descr:       object not identical to one above\n" +
                "admin-c:     ADM-TEST\n" +
                "upd-to:      dbtest@ripe.net\n" +
                "auth:        MD5-PW $1$T6B4LEdb$5IeIbPNcRJ35P1tNoXFas/  #delete\n" +
                "referral-by: DEL-MNT\n" +
                "mnt-by:      DEL-MNT\n" +
                "changed:     dbtest@ripe.net\n" +
                "source:      TEST");

        final String result = TelnetWhoisClient.queryLocalhost(QueryServer.port, "--valid-syntax DEL-MNT");

        assertThat(result, not(containsString("% 'DEL-MNT' invalid syntax")));
    }

    @Test
    public void validSyntax_wrong_queryflag_combination() {
        final String wrongFlagShowVersion = TelnetWhoisClient.queryLocalhost(QueryServer.port, "--valid-syntax --show-version 1 ADM-TEST");
        assertThat(wrongFlagShowVersion, containsString("The flags \"--valid-syntax\" and \"--show-version\" cannot be used together."));

        final String wrongFlagListVersions = TelnetWhoisClient.queryLocalhost(QueryServer.port, "--valid-syntax --list-versions 1 ADM-TEST");
        assertThat(wrongFlagListVersions, containsString("The flags \"--valid-syntax\" and \"--list-versions\" cannot be used together."));

        final String wrongFlagDiffVersions = TelnetWhoisClient.queryLocalhost(QueryServer.port, "--valid-syntax --diff-versions 1 ADM-TEST");
        assertThat(wrongFlagDiffVersions, containsString("The flags \"--valid-syntax\" and \"--diff-versions\" cannot be used together."));

        final String wrongFlagValidNovalid = TelnetWhoisClient.queryLocalhost(QueryServer.port, "--valid-syntax --no-valid-syntax 1 ADM-TEST");
        assertThat(wrongFlagValidNovalid, containsString("The flags \"--valid-syntax\" and \"--no-valid-syntax\" cannot be used together."));
    }

    @Test
    public void novalidSyntax_incorrect_syntax() {
        databaseHelper.addObject("" +
                "mntner:      DEL-MNT\n" +
                "descr:       MNTNER for test\n" +
                "descr:       object not identical to one above\n" +
                "admin-c:     ADM-TEST\n" +
                "upd-to:      dbtest_at_ripe.net\n" +
                "auth:        MD5-PW $1$T6B4LEdb$5IeIbPNcRJ35P1tNoXFas/  #delete\n" +
                "referral-by: DEL-MNT\n" +
                "mnt-by:      DEL-MNT\n" +
                "changed:     dbtest@ripe.net\n" +
                "source:      TEST");

        final String result = TelnetWhoisClient.queryLocalhost(QueryServer.port, "--no-valid-syntax DEL-MNT");
        assertThat(result, containsString("MD5-PW # Filtered"));
        assertThat(result, containsString("+312342343"));
    }

    @Test
    public void novalidSyntax_correct_syntax() {
        databaseHelper.addObject("" +
                "mntner:      DEL-MNT\n" +
                "descr:       MNTNER for test\n" +
                "descr:       object not identical to one above\n" +
                "admin-c:     ADM-TEST\n" +
                "upd-to:      dbtest@ripe.net\n" +
                "auth:        MD5-PW $1$T6B4LEdb$5IeIbPNcRJ35P1tNoXFas/  #delete\n" +
                "referral-by: DEL-MNT\n" +
                "mnt-by:      DEL-MNT\n" +
                "changed:     dbtest@ripe.net\n" +
                "source:      TEST");

        final String result = TelnetWhoisClient.queryLocalhost(QueryServer.port, "--no-valid-syntax DEL-MNT");

        assertThat(result, containsString("% 'DEL-MNT' has valid syntax"));
        assertThat(result, not(containsString("MD5-PW # Filtered")));
    }

    @Test
    public void route6_correct_rebuild() {
        databaseHelper.addObject("mntner: TEST-MNT\nupd-to: TEST-MNT");
        databaseHelper.addObject("" +
                "route6:          2aaa:6fff::/48\n" +
                "descr:           test\n" +
                "origin:          AS222\n" +
                "mnt-by:          TEST-MNT\n" +
                "changed:         test@test.net 20120428\n" +
                "source:          TEST");

        ipTreeUpdater.rebuild();
        final String query = TelnetWhoisClient.queryLocalhost(QueryServer.port, "2aaa:6fff::/48");

        assertThat(query, containsString("route6:         2aaa:6fff::/48"));
    }

    @Test
    public void autnum_status_description() {
        final String query = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-v aut-num");

        assertThat(query, containsString("status:         [generated]  [single]     [ ]"));
        assertThat(query, containsString("status"));
        assertThat(query, containsString("o ASSIGNED"));
        assertThat(query, containsString("o LEGACY"));
        assertThat(query, containsString("o OTHER"));
    }
}
