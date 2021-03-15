package net.ripe.db.whois.query.pipeline;

import net.ripe.db.whois.query.QueryMessages;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServedByHandlerTest {

    @Mock private MessageEvent messageEventMock;
    @Mock private QueryCompletedEvent queryCompletedEventMock;
    @Mock private Channel channelMock;
    @Mock private ChannelHandlerContext ctxMock;
    private ServedByHandler subject;

    @Before
    public void setup() {
        System.setProperty("instance.name", "10.0.0.0");
        subject = new ServedByHandler("");

        when(queryCompletedEventMock.getChannel()).thenReturn(channelMock);
    }

    @After
    public void after() {
        System.clearProperty("instance.name");
    }

    @Test
    public void test_handleDownstream_whois() {
        subject.handleDownstream(ctxMock, messageEventMock);
        verify(ctxMock, times(1)).sendDownstream(messageEventMock);

        subject.handleDownstream(ctxMock, queryCompletedEventMock);
        verify(ctxMock, times(1)).sendDownstream(queryCompletedEventMock);
        verify(channelMock, times(1)).write(QueryMessages.servedByNotice(any()));
    }
}
