package net.ripe.db.whois.update.dns.zonemaster;

import net.ripe.db.whois.update.domain.Update;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

// TODO: [ES] implement tests
@RunWith(MockitoJUnitRunner.class)
public class ZonemasterDnsGatewayTest {


    @Mock
    private ZonemasterRestClient restClient;
    @Mock
    private Update update;
    @InjectMocks
    private ZonemasterDnsGateway subject;


    @Test
    public void single_record() {
        // when(restClient.sendRequest()) ...

        // subject.performDnsChecks(Collections.singleton(new DnsCheckRequest(update, "ns.ripe.net", null)));
    }
}
