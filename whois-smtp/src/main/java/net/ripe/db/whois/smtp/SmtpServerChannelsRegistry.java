package net.ripe.db.whois.smtp;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFutureListener;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.DefaultEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Keeps track of all open channel and allows shutting down all currently open
 * channels cleanly.
 */
@Component
@ChannelHandler.Sharable
public class SmtpServerChannelsRegistry extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpServerChannelsRegistry.class);

    private final ChannelGroup channels = new DefaultChannelGroup(new DefaultEventExecutor());

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        // Channel automatically removes closed channels, so no need to remove on close channels from the group on close.
        channels.add(ctx.channel());
        ctx.fireChannelActive();
    }

    public int size() {
        return channels.size();
    }

    public void closeChannels() {
        LOGGER.info("Closing {} open channels.", size());
        channels.close().addListener((ChannelGroupFutureListener) future -> LOGGER.info("Closed all channels."));
    }
}
