package net.ripe.db.whois.smtp;


import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import net.ripe.db.whois.common.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;

@Component
@ChannelHandler.Sharable
public class SmtpDataHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpDataHandler.class);

    private static final AttributeKey<ByteArrayOutputStream> DATA = AttributeKey.newInstance("data");

    private final SmtpCommandHandler commandHandler;

    @Autowired
    public SmtpDataHandler(
            @Lazy final SmtpCommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
	public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
	    final byte[] bytes = getBytes((ByteBuf) msg);
	    log(ctx, bytes);
        if (isEnd(bytes)) {
            log(ctx, "End of Data");
            writeMessage(ctx.channel(), SmtpMessages.okId(ctx.channel().id().asShortText()));
            ctx.pipeline().replace("U-data-handler", "U-command-handler", commandHandler);
        } else {
            appendData(ctx, bytes);
        }
    }

    private boolean isEnd(final byte[] bytes) {
        return (bytes.length >= 2) &&
            ((bytes[0] == '.') && (bytes[1] == '\r' || bytes[1] == '\n'));
    }

    private byte[] getBytes(final ByteBuf msg) {
        if (msg.hasArray()) {
            return msg.array();
        } else {
            final int length = msg.readableBytes();
            final byte[] bytes = new byte[length];
            msg.getBytes(msg.readerIndex(), bytes);
            return bytes;
        }
    }

    @Nullable
    public byte[] getData(final Channel channel) {
        final ByteArrayOutputStream builder = channel.attr(DATA).get();
        if (builder == null || builder.size() == 0) {
            return null;
        } else {
            return builder.toByteArray();
        }
    }

    public void clearData(final Channel channel) {
        channel.attr(DATA).set(null);
    }

    private void appendData(final ChannelHandlerContext ctx, final byte[] bytes) {
        final ByteArrayOutputStream builder = ctx.channel().attr(DATA).get();
        if (builder == null) {
            final ByteArrayOutputStream newBuilder = new ByteArrayOutputStream();
            newBuilder.writeBytes(bytes);
            ctx.channel().attr(DATA).set(newBuilder);
        } else {
            builder.writeBytes(bytes);
        }
    }

    private void log(final ChannelHandlerContext ctx, final byte[] bytes) {
        LOGGER.info("Channel {} Read {} bytes: {}", ctx.channel().id(), bytes.length, byteArrayToHexString(bytes));
    }

    private void log(final ChannelHandlerContext ctx, final String msg) {
        LOGGER.info("Channel {} {}", ctx.channel().id(), msg);
    }

    private static String byteArrayToHexString(final byte[] bytes) {
        final StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (final byte b : bytes) {
            final String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private void writeMessage(final Channel channel, final Message message) {
        if (!channel.isOpen()) {
            throw new ChannelException();
        }

        channel.writeAndFlush(message);
    }
}
