package net.ripe.db.whois.query.pipeline;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.ripe.db.whois.query.QueryMessages;
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

    @Mock private ChannelPromise promiseMock;
    @Mock private QueryCompletedEvent queryCompletedEventMock;
    @Mock private Channel channelMock;
    @Mock private ChannelHandlerContext ctxMock;
    private ServedByHandler subject;

    @Before
    public void setup() {
        subject = new ServedByHandler("");
        when(ctxMock.channel()).thenReturn(channelMock);
    }

    @Test
    public void test_handleDownstream_whois() {
        String msg = "msg";
        subject.write(ctxMock, msg, promiseMock);
        verify(ctxMock, times(1)).writeAndFlush(msg);

        subject.write(ctxMock, queryCompletedEventMock, promiseMock);
        verify(ctxMock, times(1)).channel();
        verify(channelMock, times(1)).write(QueryMessages.servedByNotice(any()));
    }
}
