package net.ripe.db.whois.smtp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.smtp.SmtpResponse;
import org.springframework.stereotype.Component;

@ChannelHandler.Sharable
@Component
public class SmtpResponseEncoder extends MessageToByteEncoder<SmtpResponse> {

    @Override
    protected void encode(final ChannelHandlerContext ctx, final SmtpResponse smtpResponse, final ByteBuf out) {
        for (CharSequence detail : smtpResponse.details()) {
            out.writeBytes(String.format("%d %s\n", smtpResponse.code(), detail).getBytes());
        }
    }
}
