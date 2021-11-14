package net.ripe.db.whois.query.pipeline;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultChannelId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class QueryChannelsRegistryTest {

    @Mock private ChannelHandlerContext contextMock;
    @Mock private Channel channelMock;
    @Mock private ChannelFuture futureMock;
    @InjectMocks private QueryChannelsRegistry subject;

    @BeforeEach
    public void setup() {
        when(contextMock.channel()).thenReturn(channelMock);
        when(channelMock.closeFuture()).thenReturn(futureMock);

        when(channelMock.id()).thenReturn(DefaultChannelId.newInstance());
    }

    @Test
    public void channel_open_records_sends_upstream() {
        subject.channelActive(contextMock);

        assertThat(subject.size(), is(1));
        verify(contextMock, times(1)).fireChannelActive();
    }

    @Test
    public void service_stop_closes_channels() {
        when(channelMock.close()).thenReturn(futureMock);
        subject.channelActive(contextMock);

        subject.closeChannels();

        verify(channelMock, times(1)).close();
    }



}
