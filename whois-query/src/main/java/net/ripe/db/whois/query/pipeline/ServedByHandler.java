package net.ripe.db.whois.query.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.ripe.db.whois.query.QueryMessages;

public class ServedByHandler extends ChannelOutboundHandlerAdapter {
    private final String version;

    public ServedByHandler(final String version) {
        this.version = version;
    }

    @Override
    public void write(final ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (msg instanceof QueryCompletedEvent) {
            ctx.channel().write(QueryMessages.servedByNotice(version));
        }

        ctx.writeAndFlush(msg);
    }
}
