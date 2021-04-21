package net.ripe.db.whois.query.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.util.concurrent.TimeUnit;

/**
 * A ReadTimeoutHandler implementation that does not close the channel on timeout.
 *
 * This allows writing an error message to user before the connection eventually get's closed
 */
class KeepChannelOpenOnReadTimeoutHandler extends ReadTimeoutHandler {

    public KeepChannelOpenOnReadTimeoutHandler(long timeout, TimeUnit unit) {
        super(timeout, unit);
    }

    @Override
    protected void readTimedOut(ChannelHandlerContext ctx) throws Exception {
        ctx.fireExceptionCaught(ReadTimeoutException.INSTANCE);
    }
}
