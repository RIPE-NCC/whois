package net.ripe.db.whois.query.pipeline;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import net.ripe.db.whois.query.QueryMessages;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TermsAndConditionsHandlerTest {

    @Mock private Channel channelMock;
    @Mock private ChannelHandlerContext ctxMock;
    @InjectMocks private TermsAndConditionsHandler subject;

    @Before
    public void setup() {
        when(ctxMock.channel()).thenReturn(channelMock);
        when(ctxMock.pipeline()).thenReturn(mock(ChannelPipeline.class));
    }

    @Test
    public void test_terms_and_conditions() {
        subject.channelActive(ctxMock);

        verify(ctxMock, times(1)).channel();
        verify(channelMock, times(1)).write(QueryMessages.termsAndConditions());
    }
}
