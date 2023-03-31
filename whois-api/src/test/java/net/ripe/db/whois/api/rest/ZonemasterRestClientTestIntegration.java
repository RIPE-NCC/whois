package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.ZonemasterDummy;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.dns.DnsCheckRequest;
import net.ripe.db.whois.update.dns.zonemaster.ZonemasterRestClient;
import net.ripe.db.whois.update.dns.zonemaster.domain.GetTestResultsRequest;
import net.ripe.db.whois.update.dns.zonemaster.domain.GetTestResultsResponse;
import net.ripe.db.whois.update.dns.zonemaster.domain.StartDomainTestRequest;
import net.ripe.db.whois.update.dns.zonemaster.domain.StartDomainTestResponse;
import net.ripe.db.whois.update.dns.zonemaster.domain.VersionInfoRequest;
import net.ripe.db.whois.update.dns.zonemaster.domain.VersionInfoResponse;
import net.ripe.db.whois.update.domain.Credentials;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Paragraph;
import net.ripe.db.whois.update.domain.Update;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Tag("IntegrationTest")
public class ZonemasterRestClientTestIntegration extends AbstractIntegrationTest {

    @Autowired
    private ZonemasterDummy zonemasterDummy;
    @Autowired
    private ZonemasterRestClient zonemasterRestClient;

    @Test
    public void version_info() {
        zonemasterDummy.whenThen("1", "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":{\"zonemaster_backend\":\"1.1.0\"," +
                "\"zonemaster_engine\":\"v1.0.16\"}}\n");

        final VersionInfoResponse response = zonemasterRestClient.sendRequest(new VersionInfoRequest()).readEntity(VersionInfoResponse.class);

        assertThat(response.getId(), is("1"));
        assertThat(response.getResult().get("zonemaster_backend"), is("1.1.0"));
    }

    @Test
    public void start_domain_test_when_dsrdata_digest_contains_spaces() {
        zonemasterDummy.whenThen("1.0.10.in-addr.arpa", "{\"jsonrpc\":\"2.0\",\"id\":4,\"result\":\"b3a92c89c92414ed\"}\n");
        final RpslObject domainObject = RpslObject.parse(
            "domain:    1.0.10.in-addr.arpa\n" +
            "nserver:   ns1.ripe.net\n" +
            "nserver:   ns2.ripe.net\n" +
            "ds-rdata:  45123 10 2 76B64430CB85EA74E92184B9AF1F75482577237A4A5C23784AF9D2C1 7639088E\n" +
            "source:    TEST");
        final DnsCheckRequest request = new DnsCheckRequest(createUpdate(domainObject), domainObject.getKey().toString(), "ns1.ripe.net/10.0.1.1 ns2.ripe.net/10.0.1.2");

        final StartDomainTestResponse response = zonemasterRestClient.sendRequest(new StartDomainTestRequest(request)).readEntity(StartDomainTestResponse.class);

        assertThat(response.getResult(), is("b3a92c89c92414ed"));
    }
    @Test
    public void start_domain_test_with_trailing_dot() {
        zonemasterDummy.whenThen("1.0.10.in-addr.arpa", "{\"jsonrpc\":\"2.0\",\"id\":4,\"result\":\"b3a92c89c92414ed\"}\n");
        final RpslObject domainObject = RpslObject.parse(
                "domain:    1.0.10.in-addr.arpa.\n" +
                        "nserver:   ns1.ripe.net\n" +
                        "nserver:   ns2.ripe.net\n" +
                        "ds-rdata:  45123 10 2 76B64430CB85EA74E92184B9AF1F75482577237A4A5C23784AF9D2C1 7639088E\n" +
                        "source:    TEST");
        final DnsCheckRequest request = new DnsCheckRequest(createUpdate(domainObject), domainObject.getKey().toString(), "ns1.ripe.net/10.0.1.1 ns2.ripe.net/10.0.1.2");

        final StartDomainTestResponse response = zonemasterRestClient.sendRequest(new StartDomainTestRequest(request)).readEntity(StartDomainTestResponse.class);

        assertThat(response.getResult(), is("b3a92c89c92414ed"));
    }

    @Test
    public void start_domain_test_with_complete_response() {
        zonemasterDummy.whenThen("6", "{" +
                "\"jsonrpc\":\"2.0\"," +
                "\"id\":4," +
                "\"result\": " +
                "   {" +
                "       \"params\": " +
                "           {" +
                "               \"profile\": \"default\"," +
                "               \"priority\": 10," +
                "               \"ipv6\": true," +
                "               \"ipv4\": true," +
                "               \"client_id\": \"Whois\"," +
                "               \"nameservers\": " +
                "               [" +
                "                   {" +
                "                       \"ns\": \"ns1.serverion.nl\"" +
                "                   }," +
                "                   {" +
                "                       \"ns\": \"ns2.serverion.eu\"" +
                "                   }" +
                "               ]," +
                "               \"ds_info\": []," +
                "               \"domain\": \"142.135.185.in-addr.arpa\"," +
                "               \"queue\": 0," +
                "               \"client_version\": \"0.1-TEST\"" +
                "           }, " +
                "       \"created_at\": \"2023-03-09T13:03:31Z\"," +
                "       \"testcase_descriptions\": " +
                "           {" +
                "               \"ZONE05\": \"SOA 'expire' minimum value\"," +
                "               \"NAMESERVER01\": \"A name server should not be a recursor\"," +
                "               \"DELEGATION06\": \"Existence of SOA\"" +
                "           }, " +
                "       \"hash_id\": \"9b0c6d57681bbbe9\"," +
                "       \"creation_time\": \"2023-03-09 13:03:31\"," +
                "       \"results\": " +
                "           [" +
                "               {" +
                "                   \"level\": \"INFO\"," +
                "                   \"testcase\": \"UNSPECIFIED\"," +
                "                   \"message\": \"Using version v4.6.2 of the Zonemaster engine.\\n\"," +
                "                   \"module\": \"SYSTEM\"" +
                "               }, " +
                "               {" +
                "                   \"ns\": \"ns1.serverion.nl/37.97.242.126\"," +
                "                   \"level\": \"INFO\"," +
                "                   \"testcase\": \"NAMESERVER03\"," +
                "                   \"message\": \"AXFR not available on nameserver ns1.serverion.nl/37.97.242.126.\\n\"," +
                "                   \"module\": \"NAMESERVER\"" +
                "               }" +
                "           ]" +
                "   }" +
                "}");

        final GetTestResultsResponse response = zonemasterRestClient.sendRequest(new GetTestResultsRequest("6")).readEntity(GetTestResultsResponse.class);

        assertThat(response.getResult().getResults().size(), is(2));

        assertThat(response.getResult().getResults().get(0).getTestcase(), is("UNSPECIFIED"));
        assertThat(response.getResult().getResults().get(0).getLevel(), is("INFO"));
        assertThat(response.getResult().getResults().get(0).getMessage(), is("Using version v4.6.2 of the Zonemaster engine.\n"));
        assertThat(response.getResult().getResults().get(0).getModule(), is("SYSTEM"));

        assertThat(response.getResult().getResults().get(1).getTestcase(), is("NAMESERVER03"));
        assertThat(response.getResult().getResults().get(1).getLevel(), is("INFO"));
        assertThat(response.getResult().getResults().get(1).getMessage(), is("AXFR not available on nameserver ns1.serverion.nl/37.97.242.126.\n"));
        assertThat(response.getResult().getResults().get(1).getModule(), is("NAMESERVER"));
        assertThat(response.getResult().getResults().get(1).getNs(), is("ns1.serverion.nl/37.97.242.126"));
    }

    @Test
    public void start_domain_test_internal_error() {
        zonemasterDummy.whenThen("6", "{" +
                "\"jsonrpc\": \"2.0\"," +
                "\"id\": 6," +
                "\"error\": {" +
                    "\"code\": -32603," +
                    "\"data\": null," +
                    "\"message\": \"Internal server error\"" +
                "}" +
            "}");

        final GetTestResultsResponse response = zonemasterRestClient.sendRequest(new GetTestResultsRequest("6")).readEntity(GetTestResultsResponse.class);

        assertThat(response.getError().getMessage(), is("Internal server error"));
        assertThat(response.getError().getCode(), is(-32603));
    }

    // helper methods

    private static Update createUpdate(final RpslObject rpslObject) {
        final Paragraph paragraph = new Paragraph(rpslObject.toString(), new Credentials());
        return new Update(paragraph, Operation.UNSPECIFIED, null, rpslObject);
    }
}
