package net.ripe.db.whois.update.dns;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DnsCheckerTest {
    @Mock Update update;
    @Mock UpdateRequest updateRequest;
    @Mock UpdateContext updateContext;
    @Mock DnsGateway dnsGateway;

    @InjectMocks DnsChecker subject;

    @Test
    public void check_delete() {
        when(update.getOperation()).thenReturn(Operation.DELETE);
        subject.check(update, updateContext);

        verifyZeroInteractions(dnsGateway);
    }

    @Test
    public void check_not_domain() {
        when(update.getType()).thenReturn(ObjectType.INETNUM);
        subject.check(update, updateContext);

        verifyZeroInteractions(dnsGateway);
    }

    @Test
    public void check_override() {
        when(update.isOverride()).thenReturn(true);
        subject.check(update, updateContext);

        verifyZeroInteractions(dnsGateway);
    }

    @Test
    public void check() {
        when(update.getOperation()).thenReturn(Operation.UNSPECIFIED);
        when(update.getType()).thenReturn(ObjectType.DOMAIN);
        when(update.isOverride()).thenReturn(false);

        when(update.getSubmittedObject()).thenReturn(RpslObject.parse("" +
                "domain:          36.84.80.in-addr.arpa\n" +
                "descr:           Description\n" +
                "descr:           Description\n" +
                "admin-c:         DUMY-RIPE\n" +
                "tech-c:          DUMY-RIPE\n" +
                "zone-c:          DUMY-RIPE\n" +
                "notify:          notify@test.net\n" +
                "nserver:         ns1.test.se 80.84.32.12\n" +
                "nserver:         ns2.test.se 80.84.32.10\n" +
                "changed:         test@test.net 20010816\n" +
                "changed:         test@test.net 20121121\n" +
                "source:          RIPE\n" +
                "mnt-by:          TEST-MNT"
        ));

        when(updateContext.getCachedDnsCheckResponse(any(DnsCheckRequest.class))).thenReturn(null);
        when(dnsGateway.performDnsCheck(any(DnsCheckRequest.class))).thenReturn(new DnsCheckResponse());

        subject.check(update, updateContext);

        ArgumentCaptor<DnsCheckRequest> dnsCheckRequestArgumentCaptor = ArgumentCaptor.forClass(DnsCheckRequest.class);
        verify(dnsGateway).performDnsCheck(dnsCheckRequestArgumentCaptor.capture());

        final DnsCheckRequest dnsCheckRequest = dnsCheckRequestArgumentCaptor.getValue();
        assertThat(dnsCheckRequest.getDomain(), is("36.84.80.in-addr.arpa"));
        assertThat(dnsCheckRequest.getGlue(), is("ns1.test.se/80.84.32.12 ns2.test.se/80.84.32.10"));

        verify(updateContext, never()).addMessage(any(UpdateContainer.class), any(Message.class));
    }

    @Test
    public void checkAll() {
        when(update.getOperation()).thenReturn(Operation.UNSPECIFIED);
        when(update.getType()).thenReturn(ObjectType.DOMAIN);
        when(update.isOverride()).thenReturn(false);

        when(update.getSubmittedObject()).thenReturn(RpslObject.parse("" +
                "domain:          36.84.80.in-addr.arpa\n" +
                "descr:           Description\n" +
                "descr:           Description\n" +
                "admin-c:         DUMY-RIPE\n" +
                "tech-c:          DUMY-RIPE\n" +
                "zone-c:          DUMY-RIPE\n" +
                "notify:          notify@test.net\n" +
                "nserver:         ns1.test.se 80.84.32.12\n" +
                "nserver:         ns2.test.se 80.84.32.10\n" +
                "changed:         test@test.net 20010816\n" +
                "changed:         test@test.net 20121121\n" +
                "source:          RIPE\n" +
                "mnt-by:          TEST-MNT"
        ));

        List<Update> updateList = Lists.newArrayList();
        Set<DnsCheckRequest> dnsCheckRequests = Sets.newLinkedHashSet();
        Map<DnsCheckRequest, DnsCheckResponse> dnsResults = Maps.newHashMap();

        DnsCheckRequest dnsCheckRequest = new DnsCheckRequest("36.84.80.in-addr.arpa", "ns1.test.se/80.84.32.12 ns2.test.se/80.84.32.10");
        dnsCheckRequests.add(dnsCheckRequest);

        DnsCheckResponse dnsCheckResponse = new DnsCheckResponse();
        updateList.add(update);

        dnsResults.put(dnsCheckRequest, dnsCheckResponse);

        when(updateRequest.getUpdates()).thenReturn(updateList);
        when(updateContext.getCachedDnsCheckResponse(any(DnsCheckRequest.class))).thenReturn(null);
        when(dnsGateway.performDnsChecks(dnsCheckRequests)).thenReturn(dnsResults);

        subject.checkAll(updateRequest, updateContext);

        verify(dnsGateway).performDnsChecks(dnsCheckRequests);

        assertThat(dnsCheckRequest.getDomain(), is("36.84.80.in-addr.arpa"));
        assertThat(dnsCheckRequest.getGlue(), is("ns1.test.se/80.84.32.12 ns2.test.se/80.84.32.10"));

        verify(updateContext, never()).addMessage(any(UpdateContainer.class), any(Message.class));
    }

    @Test
    public void check_cached() {
        when(update.getOperation()).thenReturn(Operation.UNSPECIFIED);
        when(update.getType()).thenReturn(ObjectType.DOMAIN);
        when(update.isOverride()).thenReturn(false);

        when(update.getSubmittedObject()).thenReturn(RpslObject.parse("" +
                "domain:          36.84.80.in-addr.arpa\n"
        ));

        when(updateContext.getCachedDnsCheckResponse(any(DnsCheckRequest.class))).thenReturn(new DnsCheckResponse());

        subject.check(update, updateContext);

        verify(updateContext, never()).addMessage(any(UpdateContainer.class), any(Message.class));
        verifyZeroInteractions(dnsGateway);
    }

    @Test
    public void check_errors() {
        when(update.getOperation()).thenReturn(Operation.UNSPECIFIED);
        when(update.getType()).thenReturn(ObjectType.DOMAIN);
        when(update.isOverride()).thenReturn(false);

        when(update.getSubmittedObject()).thenReturn(RpslObject.parse("" +
                "domain:          36.84.80.in-addr.arpa\n"
        ));

        when(updateContext.getCachedDnsCheckResponse(any(DnsCheckRequest.class))).thenReturn(null);
        when(dnsGateway.performDnsCheck(any(DnsCheckRequest.class))).thenReturn(new DnsCheckResponse(UpdateMessages.dnsCheckTimeout()));

        subject.check(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.dnsCheckTimeout());
    }

    @Test
    public void check_cached_errors() {
        when(update.getOperation()).thenReturn(Operation.UNSPECIFIED);
        when(update.getType()).thenReturn(ObjectType.DOMAIN);
        when(update.isOverride()).thenReturn(false);

        when(update.getSubmittedObject()).thenReturn(RpslObject.parse("" +
                "domain:          36.84.80.in-addr.arpa\n"
        ));

        when(updateContext.getCachedDnsCheckResponse(any(DnsCheckRequest.class))).thenReturn(new DnsCheckResponse(UpdateMessages.dnsCheckTimeout()));

        subject.check(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.dnsCheckTimeout());
        verifyZeroInteractions(dnsGateway);
    }
}
