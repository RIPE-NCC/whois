package net.ripe.db.whois.query.pipeline;

import net.ripe.db.whois.query.domain.QueryMessages;
import net.ripe.db.whois.query.query.Query;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;

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
            ChannelFuture cf = channel.write(NEWLINE);
            if (keepAlive && !((QueryCompletedEvent) e).isForceClose()) {
                // Write syncronously
                // This write could be ignored/dropped if any "channel.close()" event happens earlier than this write()
                cf.awaitUninterruptibly();
                channel.write(QueryMessages.termsAndConditions()).awaitUninterruptibly();
            } else {
                closed = true;
                cf.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }
}
