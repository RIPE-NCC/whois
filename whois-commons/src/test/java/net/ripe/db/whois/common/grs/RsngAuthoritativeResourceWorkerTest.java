package net.ripe.db.whois.common.grs;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.HttpHeaders;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RsngAuthoritativeResourceWorkerTest {

    @Mock Client client;
    @Mock WebTarget webTarget;

    @Mock WebTarget webTargetAsn;
    @Mock Invocation.Builder builderAsn;

    @Mock WebTarget webTargetIpv4;
    @Mock Invocation.Builder builderIpv4;

    @Mock WebTarget webTargetIpv6;
    @Mock Invocation.Builder builderIpv6;

    private static final String API_KEY = "ncc-internal-api-key";

    static Logger logger = LoggerFactory.getLogger(RsngAuthoritativeResourceWorkerTest.class);

    @BeforeEach
    public void setup() {
        when(client.target(eq("rsngBaseUrl"))).thenReturn(webTarget);

        when(webTarget.path(eq("/resource-services/asn-delegations"))).thenReturn(webTargetAsn);
        when(webTargetAsn.queryParam(anyString(), any())).thenReturn(webTargetAsn);
        when(webTargetAsn.request()).thenReturn(builderAsn);
        when(builderAsn.header(eq(HttpHeaders.ACCEPT), eq("application/json"))).thenReturn(builderAsn);
        when(builderAsn.header(eq(API_KEY), eq("apikey"))).thenReturn(builderAsn);
        when(builderAsn.get(String.class)).then(invocation ->
                IOUtils.toString(getClass().getResourceAsStream("/grs/asndelegations.json"))
        );

        when(webTarget.path(eq("/resource-services/ipv4-delegations"))).thenReturn(webTargetIpv4);
        when(webTargetIpv4.queryParam(anyString(), any())).thenReturn(webTargetIpv4);
        when(webTargetIpv4.request()).thenReturn(builderIpv4);
        when(builderIpv4.header(eq(HttpHeaders.ACCEPT), eq("application/json"))).thenReturn(builderIpv4);
        when(builderIpv4.header(eq(API_KEY), eq("apikey"))).thenReturn(builderIpv4);
        when(builderIpv4.get(String.class)).then(invocation ->
                IOUtils.toString(getClass().getResourceAsStream("/grs/ipv4delegations.json"))
        );

        when(webTarget.path(eq("/resource-services/ipv6-delegations"))).thenReturn(webTargetIpv6);
        when(webTargetIpv6.queryParam(anyString(), any())).thenReturn(webTargetIpv6);
        when(webTargetIpv6.request()).thenReturn(builderIpv6);
        when(builderIpv6.header(eq(HttpHeaders.ACCEPT), eq("application/json"))).thenReturn(builderIpv6);
        when(builderIpv6.header(eq(API_KEY), eq("apikey"))).thenReturn(builderIpv6);
        when(builderIpv6.get(String.class)).then(invocation ->
                IOUtils.toString(getClass().getResourceAsStream("/grs/ipv6delegations.json"))
        );
    }

    @Test
    public void load()  {
        final AuthoritativeResource authoritativeResource = new RsngAuthoritativeResourceWorker(logger, "rsngBaseUrl", client, Executors.newCachedThreadPool(), "apikey").load();

        assertThat(authoritativeResource.isEmpty(), is(false));
        assertThat(authoritativeResource.getNrAutNums(), is(1));
        assertThat(authoritativeResource.getNrInet6nums(), is(1));
        assertThat(authoritativeResource.getNrInetnums(), is(1));

        assertThat(authoritativeResource.isMaintainedInRirSpace(ObjectType.AUT_NUM, CIString.ciString("AS7")), is(true));
        assertThat(authoritativeResource.isMaintainedInRirSpace(ObjectType.INET6NUM, CIString.ciString("2001:700::/25")), is(true));
        assertThat(authoritativeResource.isMaintainedInRirSpace(ObjectType.INETNUM, CIString.ciString("1.178.224.0-1.178.255.255")), is(true));
    }
}
