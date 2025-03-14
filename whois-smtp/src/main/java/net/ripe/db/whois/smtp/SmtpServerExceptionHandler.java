package net.ripe.db.whois.smtp;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.pipeline.ChannelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@ChannelHandler.Sharable
public class SmtpServerExceptionHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpServerExceptionHandler.class);

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable exception) {
        final Channel channel = ctx.channel();

        if (!channel.isOpen()) {
            LOGGER.info("Channel closed", exception);
            return;
        }

        switch (exception) {
            case SmtpException smtpException : {
                handleException(channel, smtpException.getMessage() + "\n\n");
                break;
            }
            default: {
                LOGGER.info("Caught exception on channel id = {}, from = {}",
                        channel.id().hashCode(),
                        ChannelUtil.getRemoteAddress(channel),
                        exception);
                handleException(channel, SmtpMessages.internalError());
            }
        }
    }

    private void handleException(final Channel channel, final String message) {
        channel.writeAndFlush(message + "\n").addListener(ChannelFutureListener.CLOSE);
    }

    private void handleException(final Channel channel, final Message message) {
        handleException(channel, message.toString());
    }

}
