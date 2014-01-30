package net.ripe.db.whois.query.pipeline;

import net.ripe.db.whois.query.QueryMessages;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TermsAndConditionsHandlerTest {

    @Mock private ChannelStateEvent channelStateEventMock;
    @Mock private Channel channelMock;
    @Mock private ChannelHandlerContext ctxMock;
    @InjectMocks private TermsAndConditionsHandler subject;

    @Before
    public void setup() {
        when(channelStateEventMock.getChannel()).thenReturn(channelMock);
    }

    @Test
    public void test_terms_and_conditions() {
        subject.channelConnected(ctxMock, channelStateEventMock);

        verify(ctxMock, times(1)).sendUpstream(channelStateEventMock);
        verify(channelMock, times(1)).write(QueryMessages.termsAndConditions());
    }
}
