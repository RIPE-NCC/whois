package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.ZonemasterDummy;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.update.dns.zonemaster.ZonemasterRestClient;
import net.ripe.db.whois.update.dns.zonemaster.domain.GetTestResultsRequest;
import net.ripe.db.whois.update.dns.zonemaster.domain.GetTestResultsResponse;
import net.ripe.db.whois.update.dns.zonemaster.domain.ZonemasterRequest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.nio.file.Files;

@Category(IntegrationTest.class)
public class ZonemasterTestIntegration extends AbstractIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZonemasterTestIntegration.class);

    @Autowired
    private ZonemasterDummy zonemasterDummy;
    @Autowired
    private ZonemasterRestClient zonemasterRestClient;

    @Test
    public void test() throws Exception {
        zonemasterDummy.whenThen(ZonemasterRequest.Method.GET_TEST_RESULTS.getMethod(), new String(Files.readAllBytes(new File("result.json").toPath())));

        LOGGER.info("response = {}", zonemasterRestClient.sendRequest(new GetTestResultsRequest("abcd")).readEntity(GetTestResultsResponse.class).toString());
    }


}
