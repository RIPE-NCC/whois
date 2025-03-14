package net.ripe.db.whois.smtp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.ripe.db.whois.common.Message;
import org.springframework.stereotype.Component;

@ChannelHandler.Sharable
@Component
public class SmtpMessageEncoder extends MessageToByteEncoder<Message> {

    @Override
    protected void encode(final ChannelHandlerContext ctx, final Message message, final ByteBuf out) {
        out.writeBytes((message.toString() + "\n").getBytes());
    }
}
