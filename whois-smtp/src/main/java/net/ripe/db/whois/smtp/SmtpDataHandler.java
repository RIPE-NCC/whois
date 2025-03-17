package net.ripe.db.whois.smtp;


import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.smtp.SmtpResponse;
import io.netty.util.AttributeKey;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.api.mail.dao.MailMessageDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Properties;

@Component
@ChannelHandler.Sharable
public class SmtpDataHandler extends ChannelInboundHandlerAdapter {

    private static final Session SESSION = Session.getInstance(new Properties());

    private static final AttributeKey<ByteArrayOutputStream> DATA = AttributeKey.newInstance("data");

    private final MailMessageDao mailMessageDao;
    private final SmtpCommandHandler commandHandler;
    private final SmtpLog smtpLog;

    @Autowired
    public SmtpDataHandler(
            final MailMessageDao mailMessageDao,
            @Lazy final SmtpCommandHandler commandHandler,
            final SmtpLog smtpLog) {
        this.mailMessageDao = mailMessageDao;
        this.commandHandler = commandHandler;
        this.smtpLog = smtpLog;
    }

    @Override
	public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (isEnd((ByteBuf) msg)) {
            smtpLog.log(ctx.channel(), "(END DATA)");
            writeMessageToDatabase(ctx.channel());
            writeResponse(ctx.channel(), SmtpResponses.okId(ctx.channel().id().asShortText()));
            ctx.pipeline().replace("data-handler", "command-handler", commandHandler);
        } else {
    	    final byte[] bytes = getBytes((ByteBuf) msg);
            appendData(ctx, bytes);
        }
    }

    private boolean isEnd(final ByteBuf msg) {
        return ((msg.readableBytes() >= 2) &&
                (msg.getByte(msg.readerIndex()) == '.') &&
                ((msg.getByte(msg.readerIndex() + 1) == '\r') ||
                 (msg.getByte(msg.readerIndex() + 1) == '\n')));
    }

    private void writeMessageToDatabase(final Channel channel) {
        final byte[] bytes = getData(channel);
        if (bytes != null && bytes.length > 0) {
            mailMessageDao.addMessage(parseMessage(bytes));
        }
    }

    private MimeMessage parseMessage(final byte[] bytes) {
        try {
            return new MimeMessage(SESSION, new ByteArrayInputStream(bytes));
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to parse message", e);
        }
    }

    private byte[] getBytes(final ByteBuf msg) {
        if ((msg.readableBytes() > 1) &&
                (msg.getByte(msg.readerIndex()) == '.')) {
            // remove extra period used for encoding
            msg.readByte();
        }
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

    private void writeResponse(final Channel channel, final SmtpResponse smtpResponse) {
        if (!channel.isOpen()) {
            throw new ChannelException();
        }

        channel.writeAndFlush(smtpResponse);
    }
}
