package net.ripe.db.whois.nrtm;

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

import java.io.IOException;

@Component
@ChannelHandler.Sharable
public class NrtmExceptionHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmExceptionHandler.class);

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable exception) throws Exception {
        final Channel channel = ctx.channel();

        if (!channel.isOpen()) {
            LOGGER.debug("Channel closed", exception);
            return;
        }

        switch (exception) {
            case NrtmException nrtmException : {
                handleException(channel, exception.getMessage() + "\n\n");
                break;
            }
            case IOException ioException : {
                LOGGER.debug("IO exception", ioException);
                break;
            }
            case null :
            default: {
                LOGGER.info("Caught exception on channel id = {}, from = {}",
                        channel.id().hashCode(),
                        ChannelUtil.getRemoteAddress(channel),
                        exception
                );
                handleException(channel, NrtmMessages.internalError());
            }
        }
    }

    private void handleException(final Channel channel, final String message) {
        channel.writeAndFlush(message).addListener(ChannelFutureListener.CLOSE);
    }

    private void handleException(final Channel channel, final Message message) {
        handleException(channel, message.toString());
    }

}
