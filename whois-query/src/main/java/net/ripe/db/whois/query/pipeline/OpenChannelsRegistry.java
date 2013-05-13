package net.ripe.db.whois.query.pipeline;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.ChannelGroupFutureListener;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * Keeps track of all open channel and allows shutting down all currently open
 * channels cleanly.
 */
@Component
@ChannelHandler.Sharable
public class OpenChannelsRegistry extends SimpleChannelUpstreamHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenChannelsRegistry.class);

    private final ChannelGroup channels = new DefaultChannelGroup();

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
        // Channel automatically removes closed channels, so no need to remove on close channels from the group on close.
        channels.add(e.getChannel());

        ctx.sendUpstream(e);
    }

    public int size() {
        return channels.size();
    }

    @PreDestroy
    public void stopService() {
        LOGGER.info("Closing {} open channels.", size());
        channels.close().addListener(new ChannelGroupFutureListener() {
            @Override
            public void operationComplete(ChannelGroupFuture future) {
                LOGGER.info("Closed all channels.");
            }
        });
    }
}
