package net.ripe.db.whois.update.dns;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContainer;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.domain.UpdateRequest;
import net.ripe.db.whois.update.log.LoggerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DnsCheckerTest {
    @Mock Update update;
    @Mock UpdateRequest updateRequest;
    @Mock UpdateContext updateContext;
    @Mock DnsGateway dnsGateway;
    @Mock LoggerContext loggerContext;

    DnsChecker subject;

    @BeforeEach
    public void setup() {
        subject = new DnsChecker(dnsGateway, loggerContext, "zonemaster");
        lenient().when(updateRequest.getUpdates()).thenReturn(Collections.singletonList(update));
    }

    @Test
    public void check_delete() {
        when(update.getOperation()).thenReturn(Operation.DELETE);
        subject.checkAll(updateRequest, updateContext);

        verifyNoMoreInteractions(dnsGateway);
    }

    @Test
    public void check_not_domain() {
        when(update.getType()).thenReturn(ObjectType.INETNUM);
        subject.checkAll(updateRequest, updateContext);

        verifyNoMoreInteractions(dnsGateway);
    }

    @Test
    public void check_override() {

        subject.checkAll(updateRequest, updateContext);

        verifyNoMoreInteractions(dnsGateway);
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
                "source:          RIPE\n" +
                "mnt-by:          TEST-MNT"
        ));

        List<Update> updateList = Lists.newArrayList();
        Set<DnsCheckRequest> dnsCheckRequests = Sets.newLinkedHashSet();
        Map<DnsCheckRequest, DnsCheckResponse> dnsResults = Maps.newHashMap();

        DnsCheckRequest dnsCheckRequest = new DnsCheckRequest(update, "36.84.80.in-addr.arpa", "ns1.test.se/80.84.32.12 ns2.test.se/80.84.32.10");
        dnsCheckRequests.add(dnsCheckRequest);

        DnsCheckResponse dnsCheckResponse = new DnsCheckResponse();
        updateList.add(update);

        dnsResults.put(dnsCheckRequest, dnsCheckResponse);

        when(updateRequest.getUpdates()).thenReturn(updateList);

        when(dnsGateway.performDnsChecks(dnsCheckRequests)).thenReturn(dnsResults);

        subject.checkAll(updateRequest, updateContext);

        verify(dnsGateway).performDnsChecks(dnsCheckRequests);

        assertThat(dnsCheckRequest.getDomain(), is("36.84.80.in-addr.arpa"));
        assertThat(dnsCheckRequest.getGlue(), is("ns1.test.se/80.84.32.12 ns2.test.se/80.84.32.10"));

        verify(updateContext, never()).addMessage(any(UpdateContainer.class), any(Message.class));
    }

    @Test
    public void check_errors() {
        when(update.getOperation()).thenReturn(Operation.UNSPECIFIED);
        when(update.getType()).thenReturn(ObjectType.DOMAIN);
        when(update.isOverride()).thenReturn(false);

        when(update.getSubmittedObject()).thenReturn(RpslObject.parse("" +
                "domain:          36.84.80.in-addr.arpa\n"
        ));

        when(dnsGateway.performDnsChecks(any(Set.class))).thenAnswer((Answer<Map<DnsCheckRequest, DnsCheckResponse>>) invocation -> {
            DnsCheckRequest arg = (DnsCheckRequest)(((Set)invocation.getArguments()[0]).iterator().next());
            return Collections.singletonMap(arg, new DnsCheckResponse(UpdateMessages.dnsCheckTimeout()));
        });

        subject.checkAll(updateRequest, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.dnsCheckTimeout());
    }

    @Test
    public void check_disabled() {
        subject = new DnsChecker(dnsGateway, loggerContext, "");

        subject.checkAll(updateRequest, updateContext);

        verifyNoMoreInteractions(dnsGateway);
    }
}
