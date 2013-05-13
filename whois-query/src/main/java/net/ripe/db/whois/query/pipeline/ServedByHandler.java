package net.ripe.db.whois.query.pipeline;

import net.ripe.db.whois.query.domain.QueryMessages;
import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;

public class ServedByHandler implements ChannelDownstreamHandler {
    private final String version;

    public ServedByHandler(final String version) {
        this.version = version;
    }

    @Override
    public void handleDownstream(final ChannelHandlerContext ctx, final ChannelEvent e) {
        if (e instanceof QueryCompletedEvent) {
            e.getChannel().write(QueryMessages.servedByNotice(version));
        }

        ctx.sendDownstream(e);
    }
}
