package net.ripe.db.whois.smtp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.smtp.SmtpResponse;
import org.springframework.stereotype.Component;

import java.util.Iterator;

@ChannelHandler.Sharable
@Component
public class SmtpResponseEncoder extends MessageToByteEncoder<SmtpResponse> {

    @Override
    protected void encode(final ChannelHandlerContext ctx, final SmtpResponse smtpResponse, final ByteBuf out) {
        for (final Iterator<CharSequence> iterator = smtpResponse.details().iterator(); iterator.hasNext(); ) {
            final CharSequence detail = iterator.next();
            out.writeBytes(String.format("%d%s%s\n", smtpResponse.code(), (iterator.hasNext() ? "-" : " "), detail).getBytes());
        }
    }
}
