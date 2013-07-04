package net.ripe.db.whois.query.integration;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.iptree.IpTreeUpdater;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.support.DummyWhoisClient;
import net.ripe.db.whois.common.support.NettyWhoisClientFactory;
import net.ripe.db.whois.common.support.WhoisClientHandler;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.query.domain.QueryMessages;
import net.ripe.db.whois.query.support.AbstractWhoisIntegrationTest;
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
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SimpleTestIntegration extends AbstractWhoisIntegrationTest {
    private static final String END_OF_HEADER = "% See http://www.ripe.net/db/support/db-terms-conditions.pdf\n\n";

    @Autowired IpTreeUpdater ipTreeUpdater;
    @Autowired DateTimeProvider dateTimeProvider;

    @Before
    public void startupWhoisServer() {
        final RpslObject person = RpslObject.parse("person: ADM-TEST\naddress: address\nphone: +312342343\nmnt-by:RIPE-NCC-HM-MNT\nadmin-c: ADM-TEST\nchanged: dbtest@ripe.net 20120707\nnic-hdl: ADM-TEST");
        final RpslObject mntner = RpslObject.parse("mntner: RIPE-NCC-HM-MNT\nmnt-by: RIPE-NCC-HM-MNT\ndescr: description\nadmin-c: ADM-TEST");
        databaseHelper.addObjects(Lists.newArrayList(person, mntner));

        databaseHelper.addObject("inetnum: 81.0.0.0 - 82.255.255.255\nnetname: NE\nmnt-by:RIPE-NCC-HM-MNT");
        databaseHelper.addObject("domain: 117.80.81.in-addr.arpa");
        databaseHelper.addObject("inetnum: 81.80.117.237 - 81.80.117.237\nnetname: NN\nstatus: OTHER");
        ipTreeUpdater.rebuild();
        queryServer.start();
    }

    @After
    public void shutdownWhoisServer() {
        queryServer.stop(true);
    }

    @Test
    public void testLoggingNonProxy() {
        final String response = DummyWhoisClient.query(QueryServer.port, "-rBGxTinetnum 81.80.117.237 - 81.80.117.237");
        assertThat(response, containsString("81.80.117.237 - 81.80.117.237"));
    }

    @Test
    public void testLocalhostAllowedToProxyRequest() {
        final String response = DummyWhoisClient.query(QueryServer.port, "-V 10.0.0.1 -rBGxTinetnum 81.80.117.237 - 81.80.117.237");

        assertThat(response, containsString("81.80.117.237 - 81.80.117.237"));
    }

    @Test
    public void testLoggingProxy() throws InterruptedException {
        final String response = DummyWhoisClient.query(QueryServer.port, "help\n");

        assertThat(response, containsString("-L"));
    }

    @Test
    public void testDoc() {
        final String response = DummyWhoisClient.query(QueryServer.port, "-v mntner");

        System.out.println(response);
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
            final String response = DummyWhoisClient.query(QueryServer.port, "\n");

            assertThat(response, containsString(QueryMessages.noSearchKeySpecified().toString()));
        }
    }

    @Test
    public void testEmptyQueriesOnMultipleLines() throws Exception {
        final String response = DummyWhoisClient.query(QueryServer.port, "\n\n\n");

        assertThat(response, containsString(QueryMessages.noSearchKeySpecified().toString()));
        assertThat(response, not(containsString(QueryMessages.internalErrorOccured().toString())));
    }

    @Test
    public void multiple_lines_unsupported() throws Exception {
        DummyWhoisClient.query(QueryServer.port, "-rwhois V-1.5 jwhois 1.0\n-rwhois V-1.5 jwhois 2.0\n-rwhois V-1.5 jwhois 3.0\n");
    }

    @Test
    public void testMultipleQueriesWithoutKeepAlive() throws Exception {
        final String response = DummyWhoisClient.query(QueryServer.port, "help\nhelp");

        assertThat(response, containsString("RIPE Database Reference Manual"));
        assertThat(response, not(containsString(QueryMessages.internalErrorOccured().toString())));
    }

    @Test
    public void testGetVersion() throws Exception {
        final String response = DummyWhoisClient.query(QueryServer.port, "-q version");

        assertThat(response, containsString("% whois-server-"));
        assertThat(response, not(containsString(QueryMessages.internalErrorOccured().toString())));
    }

    @Test
    public void testGetVersion_with_long_option() throws Exception {
        final String response = DummyWhoisClient.query(QueryServer.port, "--version");

        assertThat(response, containsString("% whois-server-"));
        assertThat(response, not(containsString(QueryMessages.internalErrorOccured().toString())));
    }

    @Test
    public void testGetInvalidInetnum() throws Exception {
        final String response = DummyWhoisClient.query(QueryServer.port, "-r -T inetnum RIPE-MNT");

        assertThat(response, containsString("%ERROR:101: no entries found"));
        assertThat(response, not(containsString(QueryMessages.internalErrorOccured().toString())));
    }

    @Test
    public void testGetMaintainer() throws Exception {
        final String response = DummyWhoisClient.query(QueryServer.port, "-r -T mntner RIPE-MNT");

        assertThat(response, containsString("%ERROR:101: no entries found"));
        assertThat(response, not(containsString(QueryMessages.internalErrorOccured().toString())));
    }

    @Test
    public void testGetMaintainer_long_version() throws Exception {
        final String response = DummyWhoisClient.query(QueryServer.port, "--no-referenced --select-types mntner RIPE-MNT");

        assertThat(response, containsString("%ERROR:101: no entries found"));
        assertThat(response, not(containsString(QueryMessages.internalErrorOccured().toString())));
    }

    @Test
    public void searchByOrganisationNameNoSearchKey() throws Exception {
        final String response = DummyWhoisClient.query(QueryServer.port, "-r -T organisation");

        assertThat(response, containsString("no search key specified"));
        assertThat(response, not(containsString(QueryMessages.internalErrorOccured().toString())));
    }

    @Test
    public void searchByAsBlockInvalidRange() throws Exception {
        final String response = DummyWhoisClient.query(QueryServer.port, "-r -T as-block AS2 - AS1");

        assertThat(response, containsString(QueryMessages.invalidSearchKey().toString()));
        assertThat(response, not(containsString(QueryMessages.internalErrorOccured().toString())));
    }

    @Test
    public void domainQueryTrailingDot() throws Exception {
        databaseHelper.addObject("domain: 9.4.e164.arpa");
        final String response = DummyWhoisClient.query(QueryServer.port, "-rT domain 9.4.e164.arpa.");

        assertThat(response, not(containsString("trailing dot in domain query")));
        assertThat(response, not(containsString(QueryMessages.internalErrorOccured().toString())));
    }

    @Test
    public void reverseDomainInvalidInverseRange() throws Exception {
        final String response = DummyWhoisClient.query(QueryServer.port, "80-28.79.198.195.in-addr.arpa");

        assertThat(response, containsString("no entries found"));
        assertThat(response, not(containsString(QueryMessages.internalErrorOccured().toString())));
    }

    @Test
    public void personQueryWithoutSearchKey() throws Exception {
        final String response = DummyWhoisClient.query(QueryServer.port, "-rT person");

        assertThat(response, containsString("no search key specified"));
        assertThat(response, not(containsString(QueryMessages.internalErrorOccured().toString())));
    }

    @Test
    public void invalidAsBlockReturnsErrorMessage() throws Exception {
        final String response = DummyWhoisClient.query(QueryServer.port, "-rT as-block AS2-AS1");

        assertThat(response, containsString(QueryMessages.invalidSearchKey().toString()));
        assertThat(response, not(containsString(QueryMessages.internalErrorOccured().toString())));
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

        final String response = DummyWhoisClient.query(QueryServer.port, "denis@ripe.net");

        assertThat(response.indexOf("nic-hdl:        DW-RIPE"), not(is(-1)));
        assertThat(response.lastIndexOf("nic-hdl:        DW-RIPE"), is(response.indexOf("nic-hdl:        DW-RIPE")));

        assertThat(response.indexOf("nic-hdl:        DW6465-RIPE"), not(is(-1)));
        assertThat(response.lastIndexOf("nic-hdl:        DW6465-RIPE"), is(response.indexOf("nic-hdl:        DW6465-RIPE")));
    }

    @Test
    public void unsupported_query() {
        final String response = DummyWhoisClient.query(QueryServer.port, "(85.115.248.176)");
        assertThat(response, containsString(QueryMessages.unsupportedQuery().toString()));
    }

    @Test
    public void more_specific_inetnum_query_including_domain_object() {
        final String response = DummyWhoisClient.query(QueryServer.port, "-r -d -M 81.80.0.0/16");
        assertThat(response, containsString("inetnum:        81.80.117.237 - 81.80.117.237"));
        assertThat(response, containsString("domain:         117.80.81.in-addr.arpa"));
    }

    @Test
    public void check_inverse_with_objecttype() {
        final String response = DummyWhoisClient.query(QueryServer.port, "-T aut-num -i member-of AS-123");
        assertThat(response, not(containsString(QueryMessages.invalidSearchKey().toString())));
        assertThat(response, containsString(QueryMessages.noResults("TEST").toString()));
    }

    @Test
    public void check_template() {
        final String response = DummyWhoisClient.query(QueryServer.port, "-t route");
        assertThat(response, containsString("" +
                "% This is the RIPE Database query service.\n" +
                "% The objects are in RPSL format.\n" +
                "%\n" +
                "% The RIPE Database is subject to Terms and Conditions.\n" +
                "% See http://www.ripe.net/db/support/db-terms-conditions.pdf\n" +
                "\n" +
                "route:          [mandatory]  [single]     [primary/lookup key]\n" +
                "descr:          [mandatory]  [multiple]   [ ]\n" +
                "origin:         [mandatory]  [single]     [primary/inverse key]\n" +
                "pingable:       [optional]   [multiple]   [ ]\n" +
                "ping-hdl:       [optional]   [multiple]   [inverse key]\n" +
                "holes:          [optional]   [multiple]   [ ]\n" +
                "org:            [optional]   [multiple]   [inverse key]\n" +
                "member-of:      [optional]   [multiple]   [inverse key]\n" +
                "inject:         [optional]   [multiple]   [ ]\n" +
                "aggr-mtd:       [optional]   [single]     [ ]\n" +
                "aggr-bndry:     [optional]   [single]     [ ]\n" +
                "export-comps:   [optional]   [single]     [ ]\n" +
                "components:     [optional]   [single]     [ ]\n" +
                "remarks:        [optional]   [multiple]   [ ]\n" +
                "notify:         [optional]   [multiple]   [inverse key]\n" +
                "mnt-lower:      [optional]   [multiple]   [inverse key]\n" +
                "mnt-routes:     [optional]   [multiple]   [inverse key]\n" +
                "mnt-by:         [mandatory]  [multiple]   [inverse key]\n" +
                "changed:        [mandatory]  [multiple]   [ ]\n" +
                "source:         [mandatory]  [single]     [ ]\n" +
                "\n" +
                "% This query was served by the RIPE Database Query Service"));
    }

    @Test
    public void check_verbose() {
        final String response = DummyWhoisClient.query(QueryServer.port, "-v route6");
        assertThat(response, containsString("" +
                "% This is the RIPE Database query service.\n" +
                "% The objects are in RPSL format.\n" +
                "%\n" +
                "% The RIPE Database is subject to Terms and Conditions.\n" +
                "% See http://www.ripe.net/db/support/db-terms-conditions.pdf\n" +
                "\n" +
                "The route6 class:\n" +
                "\n" +
                "      Each interAS route (also referred to as an interdomain route)\n" +
                "      in IPv6 domain originated by an AS is specified using a route6 \n" +
                "      object. The \"route6:\" attribute is the address prefix of the \n" +
                "      route and the \"origin:\" attribute is the AS number of the AS \n" +
                "      that originates the route into the interAS routing system.\n" +
                "\n" +
                "route6:         [mandatory]  [single]     [primary/lookup key]\n" +
                "descr:          [mandatory]  [multiple]   [ ]\n" +
                "origin:         [mandatory]  [single]     [primary/inverse key]\n" +
                "pingable:       [optional]   [multiple]   [ ]\n" +
                "ping-hdl:       [optional]   [multiple]   [inverse key]\n" +
                "holes:          [optional]   [multiple]   [ ]\n" +
                "org:            [optional]   [multiple]   [inverse key]\n" +
                "member-of:      [optional]   [multiple]   [inverse key]\n" +
                "inject:         [optional]   [multiple]   [ ]\n" +
                "aggr-mtd:       [optional]   [single]     [ ]\n" +
                "aggr-bndry:     [optional]   [single]     [ ]\n" +
                "export-comps:   [optional]   [single]     [ ]\n" +
                "components:     [optional]   [single]     [ ]\n" +
                "remarks:        [optional]   [multiple]   [ ]\n" +
                "notify:         [optional]   [multiple]   [inverse key]\n" +
                "mnt-lower:      [optional]   [multiple]   [inverse key]\n" +
                "mnt-routes:     [optional]   [multiple]   [inverse key]\n" +
                "mnt-by:         [mandatory]  [multiple]   [inverse key]\n" +
                "changed:        [mandatory]  [multiple]   [ ]\n" +
                "source:         [mandatory]  [single]     [ ]\n" +
                "\n" +
                "The content of the attributes of the route6 class are defined below:\n" +
                "\n" +
                "route6\n"));
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

        final String lookupResponse = DummyWhoisClient.query(QueryServer.port, "0/0");
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

        final String inverseLookupResponse = DummyWhoisClient.query(QueryServer.port, "-i mnt-by TEST-ROOT-MNT");
        assertThat(inverseLookupResponse, containsString("%ERROR:101: no entries found"));
    }

    @Test
    public void verbose_description() {
        final String response = DummyWhoisClient.query(QueryServer.port, "-v inetnum");
        assertThat(response, containsString("" +
                "The content of the attributes of the inetnum class are defined below:\n" +
                "\n" +
                "inetnum\n" +
                "\n" +
                "   Specifies a range of IPv4 that inetnum object presents. The ending\n" +
                "   address should be greater than the starting one.\n" +
                "\n" +
                "     <ipv4-address> - <ipv4-address>\n" +
                "\n" +
                "netname\n" +
                "\n" +
                "   The name of a range of IP address space.\n" +
                "\n" +
                "     Made up of letters, digits, the character underscore \"_\",\n" +
                "     and the character hyphen \"-\"; the first character of a name\n" +
                "     must be a letter, and the last character of a name must be a\n" +
                "     letter or a digit.\n" +
                "\n" +
                "descr\n" +
                "\n" +
                "   A short decription related to the object.\n" +
                "\n" +
                "     A sequence of ASCII characters.\n" +
                "\n" +
                "country\n" +
                "\n" +
                "   Identifies the country.\n" +
                "\n" +
                "     Valid two-letter ISO 3166 country code.\n" +
                "\n" +
                "geoloc\n" +
                "\n" +
                "   The location coordinates for the resource.\n" +
                "\n" +
                "     Location coordinates of the resource. Can take one of the following forms:\n" +
                "     \n" +
                "     [-90,90][-180,180]\n" +
                "\n" +
                "language\n" +
                "\n" +
                "   Identifies the language.\n" +
                "\n" +
                "     Valid two-letter ISO 639-1 language code.\n" +
                "\n" +
                "org\n" +
                "\n" +
                "   Points to an existing organisation object representing the entity that\n" +
                "   holds the resource.\n" +
                "\n" +
                "     The 'ORG-' string followed by 2 to 4 characters, followed by up to 5 digits\n" +
                "     followed by a source specification.  The first digit must not be \"0\".\n" +
                "     Source specification starts with \"-\" followed by source name up to\n" +
                "     9-character length.\n" +
                "\n" +
                "admin-c\n" +
                "\n" +
                "   References an on-site administrative contact.\n" +
                "\n" +
                "     From 2 to 4 characters optionally followed by up to 6 digits\n" +
                "     optionally followed by a source specification.  The first digit\n" +
                "     must not be \"0\".  Source specification starts with \"-\" followed\n" +
                "     by source name up to 9-character length.\n" +
                "\n" +
                "tech-c\n" +
                "\n" +
                "   References a technical contact.\n" +
                "\n" +
                "     From 2 to 4 characters optionally followed by up to 6 digits\n" +
                "     optionally followed by a source specification.  The first digit\n" +
                "     must not be \"0\".  Source specification starts with \"-\" followed\n" +
                "     by source name up to 9-character length.\n" +
                "\n" +
                "status\n" +
                "\n" +
                "   Specifies the status of the address range represented by inetnum or\n" +
                "   inet6num object.\n" +
                "\n" +
                "     Status can have one of these values:\n" +
                "     \n" +
                "     o ALLOCATED PA\n" +
                "     o ALLOCATED PI\n" +
                "     o ALLOCATED UNSPECIFIED\n" +
                "     o LIR-PARTITIONED PA\n" +
                "     o LIR-PARTITIONED PI\n" +
                "     o SUB-ALLOCATED PA\n" +
                "     o ASSIGNED PA\n" +
                "     o ASSIGNED PI\n" +
                "     o ASSIGNED ANYCAST\n" +
                "     o EARLY-REGISTRATION\n" +
                "     o NOT-SET\n" +
                "\n" +
                "remarks\n" +
                "\n" +
                "   Contains remarks.\n" +
                "\n" +
                "     A sequence of ASCII characters.\n" +
                "\n" +
                "notify\n" +
                "\n" +
                "   Specifies the e-mail address to which notifications of changes to an\n" +
                "   object should be sent. This attribute is filtered from the default\n" +
                "   whois output.\n" +
                "\n" +
                "     An e-mail address as defined in RFC 2822.\n" +
                "\n" +
                "mnt-by\n" +
                "\n" +
                "   Specifies the identifier of a registered mntner object used for\n" +
                "   authorisation of operations performed with the object that contains\n" +
                "   this attribute.\n" +
                "\n" +
                "     Made up of letters, digits, the character underscore \"_\",\n" +
                "     and the character hyphen \"-\"; the first character of a name\n" +
                "     must be a letter, and the last character of a name must be a\n" +
                "     letter or a digit.  The following words are reserved by\n" +
                "     RPSL, and they can not be used as names:\n" +
                "     \n" +
                "      any as-any rs-any peeras and or not atomic from to at\n" +
                "      action accept announce except refine networks into inbound\n" +
                "      outbound\n" +
                "     \n" +
                "     Names starting with certain prefixes are reserved for\n" +
                "     certain object types.  Names starting with \"as-\" are\n" +
                "     reserved for as set names.  Names starting with \"rs-\" are\n" +
                "     reserved for route set names.  Names starting with \"rtrs-\"\n" +
                "     are reserved for router set names. Names starting with\n" +
                "     \"fltr-\" are reserved for filter set names. Names starting\n" +
                "     with \"prng-\" are reserved for peering set names. Names\n" +
                "     starting with \"irt-\" are reserved for irt names.\n" +
                "\n" +
                "mnt-lower\n" +
                "\n" +
                "   Specifies the identifier of a registered mntner object used for\n" +
                "   hierarchical authorisation. Protects creation of objects directly (one\n" +
                "   level) below in the hierarchy of an object type. The authentication\n" +
                "   method of this maintainer object will then be used upon creation of\n" +
                "   any object directly below the object that contains the \"mnt-lower:\"\n" +
                "   attribute.\n" +
                "\n" +
                "     Made up of letters, digits, the character underscore \"_\",\n" +
                "     and the character hyphen \"-\"; the first character of a name\n" +
                "     must be a letter, and the last character of a name must be a\n" +
                "     letter or a digit.  The following words are reserved by\n" +
                "     RPSL, and they can not be used as names:\n" +
                "     \n" +
                "      any as-any rs-any peeras and or not atomic from to at\n" +
                "      action accept announce except refine networks into inbound\n" +
                "      outbound\n" +
                "     \n" +
                "     Names starting with certain prefixes are reserved for\n" +
                "     certain object types.  Names starting with \"as-\" are\n" +
                "     reserved for as set names.  Names starting with \"rs-\" are\n" +
                "     reserved for route set names.  Names starting with \"rtrs-\"\n" +
                "     are reserved for router set names. Names starting with\n" +
                "     \"fltr-\" are reserved for filter set names. Names starting\n" +
                "     with \"prng-\" are reserved for peering set names. Names\n" +
                "     starting with \"irt-\" are reserved for irt names.\n" +
                "\n" +
                "mnt-domains\n" +
                "\n" +
                "   Specifies the identifier of a registered mntner object used for\n" +
                "   reverse domain authorisation. Protects domain objects. The\n" +
                "   authentication method of this maintainer object will be used for any\n" +
                "   encompassing reverse domain object.\n" +
                "\n" +
                "     Made up of letters, digits, the character underscore \"_\",\n" +
                "     and the character hyphen \"-\"; the first character of a name\n" +
                "     must be a letter, and the last character of a name must be a\n" +
                "     letter or a digit.  The following words are reserved by\n" +
                "     RPSL, and they can not be used as names:\n" +
                "     \n" +
                "      any as-any rs-any peeras and or not atomic from to at\n" +
                "      action accept announce except refine networks into inbound\n" +
                "      outbound\n" +
                "     \n" +
                "     Names starting with certain prefixes are reserved for\n" +
                "     certain object types.  Names starting with \"as-\" are\n" +
                "     reserved for as set names.  Names starting with \"rs-\" are\n" +
                "     reserved for route set names.  Names starting with \"rtrs-\"\n" +
                "     are reserved for router set names. Names starting with\n" +
                "     \"fltr-\" are reserved for filter set names. Names starting\n" +
                "     with \"prng-\" are reserved for peering set names. Names\n" +
                "     starting with \"irt-\" are reserved for irt names.\n" +
                "\n" +
                "mnt-routes\n" +
                "\n" +
                "   This attribute references a maintainer object which is used in\n" +
                "   determining authorisation for the creation of route objects.\n" +
                "   After the reference to the maintainer, an optional list of\n" +
                "   prefix ranges inside of curly braces or the keyword \"ANY\" may\n" +
                "   follow. The default, when no additional set items are\n" +
                "   specified, is \"ANY\" or all more specifics. Please refer to\n" +
                "   RFC-2622 for more information.\n" +
                "\n" +
                "     <mnt-name> [ { list of <address-prefix-range> } | ANY ]\n" +
                "\n" +
                "mnt-irt\n" +
                "\n" +
                "   May appear in an inetnum or inet6num object. It points to an irt\n" +
                "   object representing a Computer Security Incident Response Team (CSIRT)\n" +
                "   that handles security incidents for the address space specified by the\n" +
                "   inetnum or inet6num object.\n" +
                "\n" +
                "     An irt name is made up of letters, digits, the character\n" +
                "     underscore \"_\", and the character hyphen \"-\"; it must start\n" +
                "     with \"irt-\", and the last character of a name must be a\n" +
                "     letter or a digit.\n" +
                "\n" +
                "changed\n" +
                "\n" +
                "   Specifies who submitted the update, and when the object was updated.\n" +
                "   This attribute is filtered from the default whois output.\n" +
                "\n" +
                "     An e-mail address as defined in RFC 2822, followed by a date\n" +
                "     in the format YYYYMMDD.\n" +
                "\n" +
                "source\n" +
                "\n" +
                "   Specifies the registry where the object is registered. Should be\n" +
                "   \"RIPE\" for the RIPE Database.\n" +
                "\n" +
                "     Made up of letters, digits, the character underscore \"_\",\n" +
                "     and the character hyphen \"-\"; the first character of a\n" +
                "     registry name must be a letter, and the last character of a\n" +
                "     registry name must be a letter or a digit.\n"));
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

        final String response = DummyWhoisClient.query(QueryServer.port, "-s TEST-GRS AS760-MNT");
        assertThat(response, containsString("" +
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
                "remarks:        ****************************\n" +
                "remarks:        * THIS OBJECT IS MODIFIED\n" +
                "remarks:        * Please note that all data that is generally regarded as personal\n" +
                "remarks:        * data has been removed from this object.\n" +
                "remarks:        * To view the original object, please query the RIPE Database at:\n" +
                "remarks:        * http://www.ripe.net/whois\n" +
                "remarks:        ****************************\n" +
                "\n" +
                "% This query was served by the RIPE Database Query Service version 0.1-TEST (UNDEFINED)\n" +
                "\n" +
                "\n"));
    }

    @Test
    public void query_updated_domain() throws Exception {
        databaseHelper.updateObject("domain: 117.80.81.in-addr.arpa\nnserver:ns.example.com\nremark:This is the current version\n");

        String response = DummyWhoisClient.query(QueryServer.port, "117.80.81.in-addr.arpa");

        assertThat(response, containsString("domain:         117.80.81.in-addr.arpa"));
        assertThat(response, containsString("This is the current version"));
    }

    @Test
    public void query_domain_versions() throws Exception {
        databaseHelper.updateObject("domain: 117.80.81.in-addr.arpa\nnserver:ns1.example.com\n");
        databaseHelper.updateObject("domain: 117.80.81.in-addr.arpa\nnserver:ns1.example.com\nnserver:ns2.example.com\n");
        databaseHelper.updateObject("domain: 117.80.81.in-addr.arpa\nnserver:ns.example.com\n");
        databaseHelper.updateObject("domain: 117.80.81.in-addr.arpa\nnserver:ns.example.com\nremark:This is the current version\n");

        String response = DummyWhoisClient.query(QueryServer.port, "--list-versions 117.80.81.in-addr.arpa");

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
        final String response = DummyWhoisClient.query(QueryServer.port, "-v role -v person");
        assertThat(response, containsString("ERROR:110: multiple use of flag"));
        assertThat(response, containsString("The flag \"-v\" cannot be used multiple times."));
    }
}
