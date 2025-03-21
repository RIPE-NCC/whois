package net.ripe.db.whois.smtp;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.smtp.SmtpResponse;
import io.netty.handler.timeout.ReadTimeoutException;
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
            return;
        }

        switch (exception) {
            case SmtpException smtpException : {
                handleException(channel, smtpException.getResponse());
                break;
            }
            case ReadTimeoutException readTimeoutException: {
                handleException(channel, SmtpResponses.timeout());
                break;
            }
            default: {
                LOGGER.error("Caught unexpected exception on channel {}, from {}",
                        channel.id(),
                        ChannelUtil.getRemoteAddress(channel));
                LOGGER.error(exception.getClass().getName(), exception);
                handleException(channel, SmtpResponses.internalError());
            }
        }
    }

    private void handleException(final Channel channel, final SmtpResponse smtpResponse) {
        channel.writeAndFlush(smtpResponse).addListener(ChannelFutureListener.CLOSE);
    }

}
