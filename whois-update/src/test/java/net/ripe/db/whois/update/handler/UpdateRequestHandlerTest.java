package net.ripe.db.whois.update.handler;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.update.dns.DnsChecker;
import net.ripe.db.whois.update.domain.Ack;
import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Origin;
import net.ripe.db.whois.update.domain.Paragraph;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateRequest;
import net.ripe.db.whois.update.domain.UpdateResponse;
import net.ripe.db.whois.update.domain.UpdateStatus;
import net.ripe.db.whois.update.handler.response.ResponseFactory;
import net.ripe.db.whois.update.log.LoggerContext;
import net.ripe.db.whois.update.log.UpdateLog;
import net.ripe.db.whois.update.sso.SsoTranslator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UpdateRequestHandlerTest {
    @Mock UpdateRequest updateRequest;
    @Mock Update update;
    @Mock UpdateContext updateContext;
    @Mock Paragraph paragraph;
    @Mock Ack ack;
    @Mock Origin origin;

    @Mock SourceContext sourceContext;
    @Mock ResponseFactory responseFactory;
    @Mock SingleUpdateHandler singleUpdateHandler;
    @Mock LoggerContext loggerContext;
    @Mock DnsChecker dnsChecker;
    @Mock UpdateNotifier updateNotifier;
    @Mock UpdateLog updateLog;
    @Mock SsoTranslator ssoTranslator;

    @InjectMocks UpdateRequestHandler subject;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setUp() {
        lenient().when(update.getOperation()).thenReturn(Operation.UNSPECIFIED);
        lenient().when(update.getSubmittedObject()).thenReturn(RpslObject.parse("mntner: DEV-ROOT-MNT"));
        lenient().when(update.getType()).thenReturn(ObjectType.MNTNER);

        lenient().when(updateRequest.getOrigin()).thenReturn(origin);
        lenient().when(updateRequest.getKeyword()).thenReturn(Keyword.NONE);
        lenient().when(updateContext.createAck()).thenReturn(ack);
    }

    @Test
    public void mntner() {
        when(updateRequest.getUpdates()).thenReturn(Lists.newArrayList(update));

        when(ack.getUpdateStatus()).thenReturn(UpdateStatus.SUCCESS);
        when(responseFactory.createAckResponse(updateContext, origin, ack)).thenReturn("ACK");

        final RpslObject maintainer = RpslObject.parse("mntner: DEV-ROOT-MNT");

        subject.handle(updateRequest, updateContext);

        verify(sourceContext).setCurrentSourceToWhoisMaster();
        verify(sourceContext).removeCurrentSource();
        verify(dnsChecker).checkAll(updateRequest, updateContext);
        verify(singleUpdateHandler).handle(origin, Keyword.NONE, update, updateContext);
        verify(updateNotifier).sendNotifications(updateRequest, updateContext);
    }

    @Test
    public void domain() {
        when(updateRequest.getUpdates()).thenReturn(Lists.newArrayList(update));

        when(ack.getUpdateStatus()).thenReturn(UpdateStatus.SUCCESS);
        when(responseFactory.createAckResponse(updateContext, origin, ack)).thenReturn("ACK");

        final RpslObject domain = RpslObject.parse("domain: 36.84.80.in-addr.arpa");

        subject.handle(updateRequest, updateContext);

        verify(sourceContext).setCurrentSourceToWhoisMaster();
        verify(sourceContext).removeCurrentSource();
        verify(dnsChecker).checkAll(updateRequest, updateContext);
        verify(singleUpdateHandler).handle(origin, Keyword.NONE, update, updateContext);
    }

    @Test
    public void domain_delete() {
        when(updateRequest.getUpdates()).thenReturn(Lists.newArrayList(update));

        when(ack.getUpdateStatus()).thenReturn(UpdateStatus.SUCCESS);
        when(responseFactory.createAckResponse(updateContext, origin, ack)).thenReturn("ACK");

        final RpslObject domain = RpslObject.parse("domain: 36.84.80.in-addr.arpa");

        subject.handle(updateRequest, updateContext);

        verify(sourceContext).setCurrentSourceToWhoisMaster();
        verify(sourceContext).removeCurrentSource();
        verify(dnsChecker).checkAll(updateRequest, updateContext);
        verify(singleUpdateHandler).handle(origin, Keyword.NONE, update, updateContext);
    }

    @Test
    public void help() {
        when(responseFactory.createHelpResponse(updateContext, origin)).thenReturn("help");
        final UpdateRequest updateRequest = new UpdateRequest(origin, Keyword.HELP, Collections.<Update>emptyList());

        final UpdateResponse response = subject.handle(updateRequest, updateContext);
        assertThat(response.getStatus(), is(UpdateStatus.SUCCESS));
        assertThat(response.getResponse(), is("help"));

        verify(sourceContext, never()).setCurrentSourceToWhoisMaster();
        verify(sourceContext, never()).removeCurrentSource();

    }
}
