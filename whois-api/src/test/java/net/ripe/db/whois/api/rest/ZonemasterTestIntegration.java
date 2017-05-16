package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.ZonemasterDummy;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.update.dns.zonemaster.ZonemasterRestClient;
import net.ripe.db.whois.update.dns.zonemaster.domain.VersionInfoRequest;
import net.ripe.db.whois.update.dns.zonemaster.domain.VersionInfoResponse;
import net.ripe.db.whois.update.dns.zonemaster.domain.ZonemasterRequest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class ZonemasterTestIntegration extends AbstractIntegrationTest {

    @Autowired
    private ZonemasterDummy zonemasterDummy;
    @Autowired
    private ZonemasterRestClient zonemasterRestClient;

    @Test
    public void version_info() throws Exception {
        zonemasterDummy.whenThen(ZonemasterRequest.Method.VERSION_INFO.getMethod(), "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":{\"zonemaster_backend\":\"1.1.0\",\"zonemaster_engine\":\"v1.0.16\"}}\n");

        final VersionInfoResponse response = zonemasterRestClient.sendRequest(new VersionInfoRequest()).readEntity(VersionInfoResponse.class);

        assertThat(response.getId(), is("1"));
        assertThat(response.getResult().get("zonemaster_backend"), is("1.1.0"));
    }
}
