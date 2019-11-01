package net.ripe.db.whois.query.pipeline;

import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.query.Query;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

public class ConnectionStateHandler extends SimpleChannelUpstreamHandler implements ChannelDownstreamHandler {

    static final ChannelBuffer NEWLINE = ChannelBuffers.wrappedBuffer(new byte[]{'\n'});

    private boolean keepAlive;
    private boolean closed;

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) {
        if (closed) {
            return;
        }

        final Query query = (Query) e.getMessage();
        final Channel channel = e.getChannel();


        if (keepAlive && query.hasOnlyKeepAlive()) {
            channel.close();
            return;
        }

        if (query.hasKeepAlive()) {
            keepAlive = true;
        }

        if (query.hasOnlyKeepAlive()) {
            channel.getPipeline().sendDownstream(new QueryCompletedEvent(channel));
        } else {
            ctx.sendUpstream(e);
        }
    }

    @Override
    public void handleDownstream(final ChannelHandlerContext ctx, final ChannelEvent e) {
        ctx.sendDownstream(e);

        if (e instanceof QueryCompletedEvent) {
            final Channel channel = e.getChannel();
            if (keepAlive && !((QueryCompletedEvent) e).isForceClose()) {
                channel.write(NEWLINE);
                channel.write(QueryMessages.termsAndConditions());
            } else {
                closed = true;
                channel.write(NEWLINE).addListener(ChannelFutureListener.CLOSE);
            }
        }
    }
}
