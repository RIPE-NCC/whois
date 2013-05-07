package net.ripe.db.whois.query.pipeline;

import com.google.common.base.Charsets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.ResponseObject;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;

@ChannelHandler.Sharable
@Component
public class WhoisEncoder extends OneToOneEncoder {
    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private static final byte[] OBJECT_TERMINATOR = {'\n'};

    @Override
    protected Object encode(final ChannelHandlerContext ctx, final Channel channel, final Object msg) throws IOException {
        if (msg instanceof ResponseObject) {
            final ChannelBuffer result = ChannelBuffers.dynamicBuffer(DEFAULT_BUFFER_SIZE);
            final ChannelBufferOutputStream out = new ChannelBufferOutputStream(result);

            ((ResponseObject) msg).writeTo(out);
            out.write(OBJECT_TERMINATOR);

            return result;
        } else if (msg instanceof Message) {
            return ChannelBuffers.wrappedBuffer(msg.toString().getBytes(Charsets.UTF_8), OBJECT_TERMINATOR);
        }

        return msg;
    }
}
