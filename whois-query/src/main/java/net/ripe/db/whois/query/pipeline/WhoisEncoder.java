package net.ripe.db.whois.query.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.ResponseObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@ChannelHandler.Sharable
@Component
public class WhoisEncoder extends MessageToMessageEncoder<Object> {
    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private static final byte[] OBJECT_TERMINATOR = {'\n'};

    @Override
    protected void encode(final ChannelHandlerContext ctx, final Object msg, final List<Object> out) throws IOException {
        if (msg instanceof ResponseObject) {
            final ByteBuf result = ctx.alloc().buffer(DEFAULT_BUFFER_SIZE);
            final ByteBufOutputStream outputStream = new ByteBufOutputStream(result);

            ((ResponseObject) msg).writeTo(outputStream);
            outputStream.write(OBJECT_TERMINATOR);

            out.add(result);
        } else if (msg instanceof Message) {
            out.add(Unpooled.wrappedBuffer(msg.toString().getBytes(StandardCharsets.UTF_8), OBJECT_TERMINATOR));
        } else {
            if (Arrays.equals((byte[]) msg, OBJECT_TERMINATOR)) {
                out.add((Unpooled.wrappedBuffer(OBJECT_TERMINATOR)));
            } else {
                out.add(msg);
            }
        }
    }
}
