package net.ripe.db.whois.query.pipeline;

import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.QueryMessages;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ServedByHandlerTest {

    @Mock private MessageEvent messageEventMock;
    @Mock private QueryCompletedEvent queryCompletedEventMock;
    @Mock private Channel channelMock;
    @Mock private ChannelHandlerContext ctxMock;
    private ServedByHandler subject;

    @Before
    public void setup() {
        subject = new ServedByHandler("");

        when(queryCompletedEventMock.getChannel()).thenReturn(channelMock);
        when(channelMock.isOpen()).thenReturn(true);
    }

    @Test
    public void test_handleDownstream_whois() {
        when(messageEventMock.getMessage()).thenReturn(new MessageObject(""));
        subject.handleDownstream(ctxMock, messageEventMock);
        verify(ctxMock, times(1)).sendDownstream(messageEventMock);

        subject.handleDownstream(ctxMock, queryCompletedEventMock);
        verify(ctxMock, times(1)).sendDownstream(queryCompletedEventMock);
        verify(channelMock, times(1)).write(QueryMessages.servedByNotice(anyString()));
    }
}
