package net.ripe.db.whois.smtp;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import net.ripe.db.whois.api.mail.dao.MailMessageDao;
import net.ripe.db.whois.common.ApplicationVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class SmtpServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpServerHandler.class);

    private static final AttributeKey<Boolean> BANNER = AttributeKey.newInstance("banner");
    private static final ChannelFutureListener LISTENER = future -> PendingWrites.decrement(future.channel());

    private final MailMessageDao mailMessageDao;
    private final ApplicationVersion applicationVersion;
    private volatile ScheduledFuture<?> scheduledFuture;

    public SmtpServerHandler(
            final MailMessageDao mailMessageDao,
            final ApplicationVersion applicationVersion) {
        this.mailMessageDao = mailMessageDao;
        this.applicationVersion = applicationVersion;
    }

    @Override
    public void channelRegistered(final ChannelHandlerContext ctx) {
        LOGGER.info("channelRegistered {}", ctx.channel().id());
        if (ctx.channel().isActive()) {
            writeMessage(ctx.channel(),  SmtpMessages.banner(applicationVersion.getVersion()));
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        LOGGER.info("channelRead {}", ctx.channel().id());
        final String queryString = msg.toString().trim();
        LOGGER.info("\tReceived message: _{}_", queryString);

        final Channel channel = ctx.channel();
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        LOGGER.info("channelActive {}", ctx.channel().id());
//        if (!ctx.channel().hasAttr(BANNER)) {
//            PendingWrites.add(ctx.channel());
//            writeMessage(ctx.channel(),  SmtpMessages.banner(applicationVersion.getVersion()));
//            ctx.channel().attr(BANNER).set(true);
//            ctx.fireChannelActive();
//        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        LOGGER.info("channelInactive {}", ctx.channel().id());
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }

        PendingWrites.remove(ctx.channel());
        ctx.fireChannelInactive();
    }

    private void writeMessage(final Channel channel, final Object message) {
        if (!channel.isOpen()) {
            throw new ChannelException();
        }

        PendingWrites.increment(channel);
        channel.writeAndFlush(message + "\n").addListener(LISTENER);
    }


    static final class PendingWrites {

        private static final AttributeKey<AtomicInteger> PENDING_WRITE_KEY = AttributeKey.newInstance("pending_write_key");

        private static final int MAX_PENDING_WRITES = 16;

        static void add(final Channel channel) {
            channel.attr(PENDING_WRITE_KEY).set(new AtomicInteger());
        }

        static void remove(final Channel channel) {
            channel.attr(PENDING_WRITE_KEY).set(null);
        }


        static void increment(final Channel channel) {
            final AtomicInteger pending = channel.attr(PENDING_WRITE_KEY).get();
            if (pending != null) {
                pending.incrementAndGet();
            }
        }

        static void decrement(final Channel channel) {
            final AtomicInteger pending = channel.attr(PENDING_WRITE_KEY).get();
            if (pending != null) {
                pending.decrementAndGet();
            }
        }

        static boolean isPending(final Channel channel) {
            final AtomicInteger pending = channel.attr(PENDING_WRITE_KEY).get();
            if (pending == null) {
                throw new ChannelException("channel removed");
            }

            return (pending.get() > MAX_PENDING_WRITES);
        }
    }
}
