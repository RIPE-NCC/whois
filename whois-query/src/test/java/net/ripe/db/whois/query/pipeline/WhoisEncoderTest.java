package net.ripe.db.whois.query.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.query.QueryMessages;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WhoisEncoderTest {

    @Mock private ChannelHandlerContext contextMock;
    @Mock private ResponseObject objectMock;
    @InjectMocks private WhoisEncoder subject;

    private ByteBuf encode(Object input) throws IOException {
        List<Object> actualResult = new ArrayList<>();
        subject.encode(contextMock, input, actualResult);

        if (actualResult == null) {
            return null;
        }

        return (ByteBuf) actualResult.get(0);
    }

    private static String toString(ByteBuf input) {
        return input.toString(StandardCharsets.UTF_8);
    }

    @Test
    public void encode_null() throws IOException {
        Object result = encode(null);

        assertThat(result, is(nullValue()));
    }

    @Test
    public void encode_Message() throws IOException {
        Message message = QueryMessages.inputTooLong();
        ByteBuf result = encode(message);

        assertThat(toString(result), is(message.toString() + "\n"));
    }

    @Test
    public void encode_ResponseObject() throws IOException {
        when(contextMock.alloc()).thenReturn(ByteBufAllocator.DEFAULT);
        ByteBuf result = encode(objectMock);

        verify(objectMock, times(1)).writeTo(any(OutputStream.class));

        assertThat(toString(result), is("\n"));
    }
}
