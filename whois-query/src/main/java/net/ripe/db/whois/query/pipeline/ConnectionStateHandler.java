package net.ripe.db.whois.query.pipeline;


import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.query.Query;


public class ConnectionStateHandler extends ChannelDuplexHandler {

    static final byte[] NEWLINE = new byte[]{'\n'};

    private boolean keepAlive;
    private boolean closed;

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (closed) {
            return;
        }

        final Query query = (Query) msg;
        final Channel channel = ctx.channel();


        if (keepAlive && query.hasOnlyKeepAlive()) {
            channel.close();
            return;
        }

        if (query.hasKeepAlive()) {
            keepAlive = true;
        }

        if (query.hasOnlyKeepAlive()) {
            channel.pipeline().write(new QueryCompletedEvent(channel));
        } else {
            ctx.fireChannelRead(msg);
        }
    }


    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        ctx.writeAndFlush(msg);

        if (msg instanceof QueryCompletedEvent) {
            final Channel channel = ((QueryCompletedEvent) msg).getChannel();
            if (keepAlive && !((QueryCompletedEvent) msg).isForceClose()) {
                channel.write(NEWLINE);
                channel.write(QueryMessages.termsAndConditions());
            } else {
                closed = true;
                ctx.write(NEWLINE).addListener(ChannelFutureListener.CLOSE);
            }
        }
    }
}
