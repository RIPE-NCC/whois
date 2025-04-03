package net.ripe.db.whois.smtp;


import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.smtp.SmtpResponse;

public interface SmtpHandler {

    default void writeResponse(final Channel channel, final SmtpResponse smtpResponse) {
        if (!channel.isOpen()) {
            throw new ChannelException();
        }

        channel.writeAndFlush(smtpResponse);
    }

    default void writeResponseAndClose(final Channel channel, final SmtpResponse smtpResponse) {
        if (!channel.isOpen()) {
            throw new ChannelException();
        }

        channel.writeAndFlush(smtpResponse).addListener(ChannelFutureListener.CLOSE);
    }
}
