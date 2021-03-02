package net.ripe.db.whois.query.pipeline;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.timeout.TimeoutException;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.pipeline.ChannelUtil;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.Collections;

public class ExceptionHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandler.class);

    private String query;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        query = msg.toString();
        ctx.fireChannelRead(msg);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.debug("Caught exception", cause);

        final Channel channel = ctx.channel();
        if (cause instanceof ClosedChannelException) {
            LOGGER.debug("Channel closed", cause);
        } else if (cause instanceof QueryException) {
            handleException(channel, ((QueryException) cause).getMessages(), ((QueryException) cause).getCompletionInfo());
        }  else if (cause.getCause() instanceof QueryException) {
            handleException(channel, ((QueryException) cause.getCause()).getMessages(), ((QueryException) cause.getCause()).getCompletionInfo());
        } else if (cause instanceof TimeoutException) {
            handleException(channel, Collections.singletonList(QueryMessages.timeout()), QueryCompletionInfo.EXCEPTION);
        } else if (cause instanceof TooLongFrameException) {
            handleException(channel, Collections.singletonList(QueryMessages.inputTooLong()), QueryCompletionInfo.EXCEPTION);
        }  else if (cause.getCause() instanceof TooLongFrameException) {
            handleException(channel, Collections.singletonList(QueryMessages.inputTooLong()), QueryCompletionInfo.EXCEPTION);
        }  else if (cause instanceof IOException) {
            handleException(channel, Collections.<Message>emptyList(), QueryCompletionInfo.EXCEPTION);
        } else if (cause instanceof DataAccessException) {
            LOGGER.error("Caught exception on channel id = {}, from = {} for query = {}\n{}",
                    channel.id().hashCode(),
                    ChannelUtil.getRemoteAddress(channel),
                    query,
                    cause.toString());

            handleException(channel, Collections.singletonList(QueryMessages.internalErroroccurred()), QueryCompletionInfo.EXCEPTION);
        } else {
            LOGGER.error("Caught exception on channel id = {}, from = {} for query = {}",
                    channel.id().hashCode(),
                    ChannelUtil.getRemoteAddress(channel),
                    query,
                    cause);

            handleException(channel, Collections.singletonList(QueryMessages.internalErroroccurred()), QueryCompletionInfo.EXCEPTION);
        }
    }

    private void handleException(final Channel channel, final Iterable<Message> messages, final QueryCompletionInfo completionInfo) {
        if (channel.isOpen()) {
            for (final Message message : messages) {
                channel.write(message);
            }
        }

        channel.pipeline().write(new QueryCompletedEvent(channel, completionInfo));
    }
}
