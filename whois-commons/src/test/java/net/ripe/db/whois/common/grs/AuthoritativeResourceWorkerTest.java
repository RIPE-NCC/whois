package net.ripe.db.whois.common.grs;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import java.util.concurrent.Executors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthoritativeResourceWorkerTest {

    @Mock Client client;
    @Mock WebTarget webTarget;

    @Mock WebTarget webTargetAsn;
    @Mock Invocation.Builder builderAsn;

    @Mock WebTarget webTargetIpv4;
    @Mock Invocation.Builder builderIpv4;

    @Mock WebTarget webTargetIpv6;
    @Mock Invocation.Builder builderIpv6;

    static Logger logger = LoggerFactory.getLogger(AuthoritativeResourceWorkerTest.class);

    @Before
    public void setup() {
        when(client.target(eq("rsngBaseUrl"))).thenReturn(webTarget);

        when(webTarget.path(eq("/resource-services/asn-delegations"))).thenReturn(webTargetAsn);
        when(webTargetAsn.queryParam(anyString(), any())).thenReturn(webTargetAsn);
        when(webTargetAsn.request()).thenReturn(builderAsn);
        when(builderAsn.header(eq(HttpHeaders.ACCEPT), eq("application/json"))).thenReturn(builderAsn);
        when(builderAsn.header(eq("X-API_KEY"), eq("apikey"))).thenReturn(builderAsn);
        when(builderAsn.get(String.class)).then(invocation ->
                IOUtils.toString(getClass().getResourceAsStream("/grs/asndelegations.json"))
        );

        when(webTarget.path(eq("/resource-services/ipv4-delegations"))).thenReturn(webTargetIpv4);
        when(webTargetIpv4.queryParam(anyString(), any())).thenReturn(webTargetIpv4);
        when(webTargetIpv4.request()).thenReturn(builderIpv4);
        when(builderIpv4.header(eq(HttpHeaders.ACCEPT), eq("application/json"))).thenReturn(builderIpv4);
        when(builderIpv4.header(eq("X-API_KEY"), eq("apikey"))).thenReturn(builderIpv4);
        when(builderIpv4.get(String.class)).then(invocation ->
                IOUtils.toString(getClass().getResourceAsStream("/grs/ipv4delegations.json"))
        );

        when(webTarget.path(eq("/resource-services/ipv6-delegations"))).thenReturn(webTargetIpv6);
        when(webTargetIpv6.queryParam(anyString(), any())).thenReturn(webTargetIpv6);
        when(webTargetIpv6.request()).thenReturn(builderIpv6);
        when(builderIpv6.header(eq(HttpHeaders.ACCEPT), eq("application/json"))).thenReturn(builderIpv6);
        when(builderIpv6.header(eq("X-API_KEY"), eq("apikey"))).thenReturn(builderIpv6);
        when(builderIpv6.get(String.class)).then(invocation ->
                IOUtils.toString(getClass().getResourceAsStream("/grs/ipv6delegations.json"))
        );
    }

    @Test
    public void load()  {
        final AuthoritativeResource authoritativeResource = new AuthoritativeResourceWorker(logger, "rsngBaseUrl", client, Executors.newCachedThreadPool(), "apikey").load();

        assertFalse(authoritativeResource.isEmpty());
        assertThat(authoritativeResource.getNrAutNums(), is(1));
        assertThat(authoritativeResource.getNrInet6nums(), is(1));
        assertThat(authoritativeResource.getNrInetnums(), is(1));

        assertTrue(authoritativeResource.isMaintainedInRirSpace(ObjectType.AUT_NUM, CIString.ciString("AS7")));
        assertTrue(authoritativeResource.isMaintainedInRirSpace(ObjectType.INET6NUM, CIString.ciString("2001:700::/25")));
        assertTrue(authoritativeResource.isMaintainedInRirSpace(ObjectType.INETNUM, CIString.ciString("1.178.224.0-1.178.255.255")));
    }
}
