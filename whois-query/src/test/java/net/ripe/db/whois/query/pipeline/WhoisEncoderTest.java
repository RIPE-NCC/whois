package net.ripe.db.whois.query.pipeline;

import com.google.common.base.Charsets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.query.domain.QueryMessages;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.OutputStream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class WhoisEncoderTest {

    @Mock private ChannelHandlerContext contextMock;
    @Mock private Channel channelMock;
    @Mock private ResponseObject objectMock;
    @InjectMocks private WhoisEncoder subject;

    private ChannelBuffer encode(Object input) throws IOException {
        Object result = subject.encode(contextMock, channelMock, input);

        if (result == null) {
            return null;
        }

        return (ChannelBuffer) result;
    }

    private static String toString(ChannelBuffer input) {
        return input.toString(Charsets.UTF_8);
    }

    @Test
    public void encode_null() throws IOException {
        Object result = encode(null);

        assertThat(result, is(nullValue()));
    }

    @Test
    public void encode_Message() throws IOException {
        Message message = QueryMessages.inputTooLong();
        ChannelBuffer result = encode(message);

        assertThat(toString(result), is(message.toString() + "\n"));
    }

    @Test
    public void encode_ResponseObject() throws IOException {
        ChannelBuffer result = encode(objectMock);

        verify(objectMock, times(1)).writeTo(any(OutputStream.class));

        assertThat(toString(result), is("\n"));
    }
}
