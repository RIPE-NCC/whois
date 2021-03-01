package net.ripe.db.whois.nrtm;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.ripe.db.whois.common.pipeline.ChannelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@ChannelHandler.Sharable
public class NrtmExceptionHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmExceptionHandler.class);

    static final String MESSAGE = "internal error occurred.";

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable exception) throws Exception {
        final Channel channel = ctx.channel();

        if (!channel.isOpen()) {
            return;
        }

        if (exception instanceof IllegalArgumentException) {
            // expected query exception
            channel.writeAndFlush(exception.getMessage() + "\n\n").addListener(ChannelFutureListener.CLOSE);
        } else if (exception instanceof IOException) {
            LOGGER.debug("IO exception", exception);
        } else {
//            TODO [DA] difference between asLongText and asShortText
            LOGGER.error("Caught exception on channel id = {}, from = {}",
                    channel.id().hashCode(),
                    ChannelUtil.getRemoteAddress(channel),
                    exception
            );

            channel.write(MESSAGE).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
