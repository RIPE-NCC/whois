package net.ripe.db.legacy;

import net.ripe.db.whois.api.rest.client.RestClient;
import net.ripe.db.whois.api.rest.client.RestClientTarget;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
    (1) input line: 137.191.192.0/20,/21,/22,/23,/24
        should map to: 137.191.192.0 - 137.191.222.255 [7936]
            137.191.192/20      137.191.192.0 - 137.191.207.255 [4096]
            137.191.208/21      137.191.208.0 - 137.191.215.255 [2048]
            137.191.216/22      137.191.216.0 - 137.191.219.255 [1024]
            137.191.220/23      137.191.220.0 - 137.191.221.255 [512]
            137.191.222/24      137.191.222.0 - 137.191.222.255 [256]
    (2) input line: 128.141.0.0/16,/16
        should map to: 128.141.0.0 - 128.142.255.255
 */
@RunWith(MockitoJUnitRunner.class)
public class SetLegacyStatusTest {

    @Mock RestClient restClient;
    @Mock RestClientTarget restClientTarget;
    private SetLegacyStatus setLegacyStatus;

    @Before
    public void setup() throws Exception {
        when(restClient.request()).thenReturn(restClientTarget);
        when(restClientTarget.addParam(any(String.class), any(String.class))).thenReturn(restClientTarget);
        when(restClientTarget.addParams(any(String.class), org.mockito.Matchers.<String>anyVararg())).thenReturn(restClientTarget);

        final Path tempFile = Files.createTempFile("test", "csv");
        final String overrideUsername = "dbint";
        final String overridePassword = "dbint";
        this.setLegacyStatus = new SetLegacyStatus(tempFile.getFileName().toString(), overrideUsername, overridePassword, restClient, true);
    }

    @Test
    public void shouldGiveRpslObjectForCsl() throws Exception {
        when(restClientTarget.search()).thenReturn(Collections.singleton(RpslObject.parse("inetnum: 137.191.192.0 - 137.191.222.255")));

        setLegacyStatus.lookupTopLevelIpv4ResourceFromCsl("137.191.192.0/20,/21,/22,/23,/24");

        verify(restClientTarget).addParam("query-string", "137.191.192.0 - 137.191.222.255");
    }

    @Test
    public void shouldNotGiveIpIntervalForCsl() throws Exception {
        final RpslObject rpslObject = setLegacyStatus.lookupTopLevelIpv4ResourceFromCsl("128.141.0.0/16,/17");

        verify(restClientTarget).addParam("query-string", "128.141.0.0 - 128.142.127.255");
        assertThat(rpslObject, nullValue());
    }

    @Test
    public void shouldGiveIntervalForInetnum() throws Exception {
        when(restClientTarget.search()).thenReturn(Collections.singleton(RpslObject.parse("inetnum: 128.130.0.0 - 128.131.255.255")));

        setLegacyStatus.lookupTopLevelIp4Resource("128.130.0.0/15");

        verify(restClientTarget).addParam("query-string", "128.130.0.0 - 128.131.255.255");
    }
}
