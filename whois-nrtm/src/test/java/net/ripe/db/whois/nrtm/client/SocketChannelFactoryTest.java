package net.ripe.db.whois.nrtm.client;

import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SocketChannelFactoryTest {

    @Mock private SocketChannel socketChannel;

    @Test
    public void read_one_line() throws Exception {
        mockRead("aaa\n");

        SocketChannelFactory.Reader reader = SocketChannelFactory.createReader(socketChannel);

        assertThat(reader.readLine(), is("aaa"));
    }

    @Test
    public void read_empty_line() throws Exception {
        mockRead("\n");

        SocketChannelFactory.Reader reader = SocketChannelFactory.createReader(socketChannel);

        assertThat(reader.readLine(), is(""));
    }

    @Test
    public void read_multiple_lines() throws Exception {
        mockRead("aaa\nbbb\nccc\n");

        SocketChannelFactory.Reader reader = SocketChannelFactory.createReader(socketChannel);

        assertThat(reader.readLine(), is("aaa"));
        assertThat(reader.readLine(), is("bbb"));
        assertThat(reader.readLine(), is("ccc"));
    }

    @Test
    public void write_line() throws Exception {
        mockWrite("aaa\n");

        SocketChannelFactory.Writer writer = SocketChannelFactory.createWriter(socketChannel);

        writer.writeLine("aaa");
    }

    private void mockRead(final String data) throws IOException {
        when(socketChannel.read(any(ByteBuffer.class))).thenAnswer(new Answer<Integer> () {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                byte[] dataBytes = data.getBytes();
                Object[] args = invocation.getArguments();
                final ByteBuffer byteBuffer = (ByteBuffer)args[0];
                // update buffer for read
                byteBuffer.put(dataBytes, 0, dataBytes.length);
                return dataBytes.length;
            }
        });
    }

    private void mockWrite(final String data) throws IOException {
        when(socketChannel.write(any(ByteBuffer.class))).thenAnswer(new Answer<Integer> () {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                final ByteBuffer byteBuffer = (ByteBuffer)args[0];
                // check bytes written to buffer
                final byte[] dataBytes = new byte[byteBuffer.remaining()];
                byteBuffer.get(dataBytes);
                assertThat(new String(dataBytes), is(data));
                return data.length();
            }
        });
    }
}
