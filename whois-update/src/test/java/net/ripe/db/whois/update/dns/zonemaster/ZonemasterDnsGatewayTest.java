package net.ripe.db.whois.update.dns.zonemaster;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.ApplicationVersion;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.dns.DnsCheckRequest;
import net.ripe.db.whois.update.dns.DnsCheckResponse;
import net.ripe.db.whois.update.dns.zonemaster.domain.GetTestResultsResponse;
import net.ripe.db.whois.update.dns.zonemaster.domain.StartDomainTestResponse;
import net.ripe.db.whois.update.dns.zonemaster.domain.TestProgressResponse;
import net.ripe.db.whois.update.dns.zonemaster.domain.ZonemasterRequest;
import net.ripe.db.whois.update.domain.Update;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// TODO: [ES] slow unit tests (takes ~30s)
@ExtendWith(MockitoExtension.class)
public class ZonemasterDnsGatewayTest {

    @Mock
    private ZonemasterRestClient restClient;
    @Mock
    private Update update;
    @Mock
    private Response response;
    @Mock
    private StartDomainTestResponse startDomainTestResponse;
    @Mock
    private TestProgressResponse testProgressResponse;
    @Mock
    private GetTestResultsResponse getTestResultsResponse;
    @Mock
    private GetTestResultsResponse.Result result;
    @Mock
    private GetTestResultsResponse.Result.Message message;
    @Mock
    private ApplicationVersion applicationVersion;
    @InjectMocks
    private ZonemasterDnsGateway subject;

    @BeforeEach
    public void setup() {
        when(restClient.sendRequest(any(ZonemasterRequest.class))).thenReturn(response);
        when(response.readEntity(StartDomainTestResponse.class)).thenReturn(startDomainTestResponse);
        when(response.readEntity(TestProgressResponse.class)).thenReturn(testProgressResponse);
        when(response.readEntity(GetTestResultsResponse.class)).thenReturn(getTestResultsResponse);
        when(getTestResultsResponse.getResult()).thenReturn(result);
        when(result.getResults()).thenReturn(Lists.newArrayList());
        when(applicationVersion.getVersion()).thenReturn("1.0");
    }

    @Test
    public void single_record_with_error_message() {
        mock(RpslObject.parse("domain: 22.0.193.in-addr.arpa"));
        when(startDomainTestResponse.getResult()).thenReturn("1");
        when(testProgressResponse.getResult()).thenReturn("50").thenReturn("100");
        when(result.getResults()).thenReturn(Lists.newArrayList(message));
        when(message.getMessage()).thenReturn("check failed");
        when(message.getLevel()).thenReturn("ERROR");

        final Map<DnsCheckRequest, DnsCheckResponse> response =
            subject.performDnsChecks(
                Sets.newHashSet(new DnsCheckRequest(update, "1.ripe.net", null))
            );

        assertThat(response.size(), is(1));
        assertThat(response.values(), hasSize(1));
        final List<Message> messages = response.values().iterator().next().getMessages();
        assertThat(messages, contains(new Message(Messages.Type.ERROR, "check failed")));
    }

    @Test
    public void odd_number_of_domain_objects_above_threshold() {
        mock(RpslObject.parse("domain: 22.0.193.in-addr.arpa"));
        when(startDomainTestResponse.getResult()).thenReturn("1");
        when(testProgressResponse.getResult()).thenReturn("100");

        final Map<DnsCheckRequest, DnsCheckResponse> response =
            subject.performDnsChecks(Sets.newHashSet(
                new DnsCheckRequest(update, "1.ripe.net", null),
                new DnsCheckRequest(update, "2.ripe.net", null),
                new DnsCheckRequest(update, "3.ripe.net", null),
                new DnsCheckRequest(update, "4.ripe.net", null),
                new DnsCheckRequest(update, "5.ripe.net", null),
                new DnsCheckRequest(update, "6.ripe.net", null),
                new DnsCheckRequest(update, "7.ripe.net", null),
                new DnsCheckRequest(update, "8.ripe.net", null),
                new DnsCheckRequest(update, "9.ripe.net", null),
                new DnsCheckRequest(update, "10.ripe.net", null),
                new DnsCheckRequest(update, "11.ripe.net", null)
            ));

        assertThat(response.values(), hasSize(11));
    }

    @Test
    public void even_number_of_domain_objects_above_threshold() {
        mock(RpslObject.parse("domain: 22.0.193.in-addr.arpa"));
        when(startDomainTestResponse.getResult()).thenReturn("1");
        when(testProgressResponse.getResult()).thenReturn("100");

        final Map<DnsCheckRequest, DnsCheckResponse> response =
            subject.performDnsChecks(Sets.newHashSet(
                new DnsCheckRequest(update, "1.ripe.net", null),
                new DnsCheckRequest(update, "2.ripe.net", null),
                new DnsCheckRequest(update, "3.ripe.net", null),
                new DnsCheckRequest(update, "4.ripe.net", null),
                new DnsCheckRequest(update, "5.ripe.net", null),
                new DnsCheckRequest(update, "6.ripe.net", null),
                new DnsCheckRequest(update, "7.ripe.net", null),
                new DnsCheckRequest(update, "8.ripe.net", null),
                new DnsCheckRequest(update, "9.ripe.net", null),
                new DnsCheckRequest(update, "10.ripe.net", null),
                new DnsCheckRequest(update, "11.ripe.net", null),
                new DnsCheckRequest(update, "12.ripe.net", null)
            ));

        assertThat(response.values(), hasSize(12));
    }

    // helper methods

    private void mock(final RpslObject rpslObject) {
        when(update.getSubmittedObject()).thenReturn(rpslObject);
    }
}
