package net.ripe.db.whois.query.pipeline;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.ripe.db.whois.query.QueryMessages;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ServedByHandlerTest {

    @Mock private ChannelPromise promiseMock;
    @Mock private QueryCompletedEvent queryCompletedEventMock;
    @Mock private Channel channelMock;
    @Mock private ChannelHandlerContext ctxMock;
    private ServedByHandler subject;

    @BeforeEach
    public void setup() {
        System.setProperty("instance.name", "10.0.0.0");
        subject = new ServedByHandler("");
        when(ctxMock.channel()).thenReturn(channelMock);
    }

    @AfterEach
    public void after() {
        System.clearProperty("instance.name");
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
