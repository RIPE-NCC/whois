package net.ripe.db.whois.query.pipeline;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.pipeline.ChannelUtil;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import net.ripe.db.whois.query.domain.ResponseHandler;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.query.Query;


/**
 * The worker threads are asynchronously pushing data down the Netty pipeline.
 * Make sure IO threads can handle the flow.
 */
public class WhoisServerHandler extends ChannelInboundHandlerAdapter {
    private final QueryHandler queryHandler;
    private boolean closed;

    public WhoisServerHandler(final QueryHandler queryHandler) {
        this.queryHandler = queryHandler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        final Query query = (Query) msg;
        final Channel channel = ctx.channel();
        queryHandler.streamResults(query, ChannelUtil.getRemoteAddress(channel), channel.id().hashCode(), new ResponseHandler() {
            @Override
            public String getApi() {
                return "QRY";
            }

            @Override
            public void handle(final ResponseObject responseObject) {
                if (closed) { // Prevent hammering a closed channel
                    throw new QueryException(QueryCompletionInfo.DISCONNECTED);
                }
                channel.write(responseObject);
            }
        });

        ctx.pipeline().write(new QueryCompletedEvent(channel));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        closed = true;
        ctx.fireChannelInactive();
    }
}
