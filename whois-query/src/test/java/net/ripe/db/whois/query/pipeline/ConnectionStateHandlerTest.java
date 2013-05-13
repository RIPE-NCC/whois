package net.ripe.db.whois.query.pipeline;

import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryMessages;
import net.ripe.db.whois.query.query.Query;
import org.jboss.netty.channel.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionStateHandlerTest {

    @Mock private MessageEvent messageMock;
    @Mock private Channel channelMock;
    @Mock private ChannelPipeline pipelineMock;
    @Mock private ChannelFuture futureMock;
    @Mock private ChannelHandlerContext contextMock;
    @InjectMocks private ConnectionStateHandler subject;

    @Before
    public void setup() {
        when(messageMock.getChannel()).thenReturn(channelMock);
        when(messageMock.getFuture()).thenReturn(futureMock);
        when(channelMock.isOpen()).thenReturn(true);
        when(channelMock.write(anyObject())).thenReturn(futureMock);
        when(channelMock.getPipeline()).thenReturn(pipelineMock);
    }

    @Test
    public void sendingNoKFlagShouldNotEnableKeepAlive() throws Exception {
        when(messageMock.getMessage()).thenReturn(Query.parse("help"));

        subject.handleUpstream(contextMock, messageMock);
        verify(contextMock, times(1)).sendUpstream(messageMock);

        subject.handleDownstream(contextMock, new QueryCompletedEvent(channelMock));
        verify(channelMock, times(1)).write(ConnectionStateHandler.NEWLINE);
        verify(futureMock, times(1)).addListener(ChannelFutureListener.CLOSE);
    }

    @Test
    public void sendingNoKFlagButConnectionStateIsKeepAliveKeepItThatWay() throws Exception {
        when(messageMock.getMessage()).thenReturn(Query.parse("-k"));
        subject.handleUpstream(contextMock, messageMock);

        when(messageMock.getMessage()).thenReturn(Query.parse("help"));
        subject.handleUpstream(contextMock, messageMock);
        verify(contextMock, times(1)).sendUpstream(messageMock);


        subject.handleDownstream(contextMock, new QueryCompletedEvent(channelMock));
        verify(channelMock, times(1)).write(QueryMessages.termsAndConditions());
        verify(futureMock, times(0)).addListener(ChannelFutureListener.CLOSE);
    }

    @Test
    public void firstSingleKShouldKeepConnectionOpen() throws Exception {
        when(messageMock.getMessage()).thenReturn(Query.parse("-k"));

        subject.handleUpstream(contextMock, messageMock);
        verify(contextMock, times(0)).sendUpstream(messageMock);

        subject.handleDownstream(contextMock, new QueryCompletedEvent(channelMock));
        verify(futureMock, times(0)).addListener(ChannelFutureListener.CLOSE);
    }

    @Test
    public void firstKWithArgumentsShouldKeepConnectionOpen() throws Exception {
        when(messageMock.getMessage()).thenReturn(Query.parse("-k -r -T inetnum 10.0.0.0"));

        subject.handleUpstream(contextMock, messageMock);
        verify(contextMock, times(1)).sendUpstream(messageMock);

        when(contextMock.getAttachment()).thenReturn(Boolean.TRUE);
        subject.handleDownstream(contextMock, new QueryCompletedEvent(channelMock));
        verify(futureMock, times(0)).addListener(ChannelFutureListener.CLOSE);
        verify(channelMock, times(2)).write(any());
    }

    @Test
    public void secondSingleKShouldCloseConnection() throws Exception {
        when(messageMock.getMessage()).thenReturn(Query.parse("-k"));
        subject.handleUpstream(contextMock, messageMock);
        verify(channelMock, times(0)).close();

        subject.handleUpstream(contextMock, messageMock);
        verify(channelMock, times(1)).close();
    }

    @Test
    public void secondKWithArgumentsShouldKeepConnectionOpen() throws Exception {
        when(messageMock.getMessage()).thenReturn(Query.parse("-k -r -T inetnum 10.0.0.0"));

        subject.handleUpstream(contextMock, messageMock);
        verify(contextMock, times(1)).sendUpstream(messageMock);

        subject.handleDownstream(contextMock, new QueryCompletedEvent(channelMock));
        verify(futureMock, times(0)).addListener(ChannelFutureListener.CLOSE);
    }

    @Test
    public void forceCloseShouldCloseConnection() throws Exception {
        when(messageMock.getMessage()).thenReturn(Query.parse("-k -r -T inetnum 10.0.0.0"));

        subject.handleUpstream(contextMock, messageMock);
        verify(contextMock, times(1)).sendUpstream(messageMock);

        subject.handleDownstream(contextMock, new QueryCompletedEvent(channelMock, QueryCompletionInfo.DISCONNECTED));
        verify(channelMock, times(0)).write(QueryMessages.termsAndConditions());
        verify(channelMock, times(1)).write(ConnectionStateHandler.NEWLINE);
        verify(futureMock, times(1)).addListener(ChannelFutureListener.CLOSE);
    }

    @Test
    public void dontActOnDownstreamNonQueryCompletedEvents() throws Exception {
        subject.handleDownstream(contextMock, null);

        verify(channelMock, times(0)).write(any());
    }
}
