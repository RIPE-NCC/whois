package net.ripe.db.whois.smtp;


import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.api.mail.dao.MailMessageDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;

@Component
@ChannelHandler.Sharable
public class SmtpDataHandler extends ChannelInboundHandlerAdapter implements SmtpHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpDataHandler.class);

    private static final AttributeKey<ByteArrayOutputStream> DATA = AttributeKey.newInstance("data");

    private final MailMessageDao mailMessageDao;
    private final SmtpCommandHandler commandHandler;
    private final SmtpLog smtpLog;
    private final SmtpRawLog smtpRawLog;
    private final int maximumSize;
    private final InternetAddress smtpFrom;

    @Autowired
    public SmtpDataHandler(
            final MailMessageDao mailMessageDao,
            @Lazy final SmtpCommandHandler commandHandler,
            @Value("${mail.smtp.server.maximum.size:0}") final int maximumSize,
            @Value("${mail.smtp.from:}") final String smtpFrom,
            final SmtpLog smtpLog,
            final SmtpRawLog smtpRawLog) {
        this.mailMessageDao = mailMessageDao;
        this.commandHandler = commandHandler;
        this.smtpLog = smtpLog;
        this.smtpRawLog = smtpRawLog;
        this.maximumSize = maximumSize;
        this.smtpFrom = MimeUtility.parseAddress(smtpFrom);
    }

    @Override
	public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        try {
            if (isEndMessage((ByteBuf) msg)) {
                if ((maximumSize > 0) && (getMessageLength(ctx.channel()) > maximumSize)) {
                    smtpLog.log(ctx.channel(), "(END DATA: TOO LONG)");
                    writeResponse(ctx.channel(), SmtpResponses.sizeExceeded());
                } else {
                    smtpLog.log(ctx.channel(), "(END DATA)");
                    smtpRawLog.log(getMessage(ctx.channel()));
                    final MimeMessage mimeMessage = MimeUtility.parseMessage(getMessage(ctx.channel()));
                    if (isMessageFromOurselves(mimeMessage)) {
                        writeResponse(ctx.channel(), SmtpResponses.refusingMessageFrom(smtpFrom.getAddress()));
                    } else {
                        writeMessageToDatabase(mimeMessage);
                        writeResponse(ctx.channel(), SmtpResponses.okId(ctx.channel().id().asShortText()));
                    }
                }
                clearMessage(ctx.channel());
                switchToCommandHandler(ctx);
            } else {
                final byte[] bytes = readMessageFromChannel((ByteBuf) msg);
                appendToMessage(ctx, bytes);
            }
        } finally {
            ((ByteBuf)msg).release();
        }
    }

    private boolean isEndMessage(final ByteBuf msg) {
        return ((msg.readableBytes() >= 2) &&
                (msg.getByte(msg.readerIndex()) == '.') &&
                ((msg.getByte(msg.readerIndex() + 1) == '\r') ||
                 (msg.getByte(msg.readerIndex() + 1) == '\n')));
    }

    private void writeMessageToDatabase(final MimeMessage mimeMessage) {
        if (mimeMessage != null) {
            mailMessageDao.addMessage(mimeMessage);
        }
    }

    private static byte[] readMessageFromChannel(final ByteBuf msg) {
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
    public static byte[] getMessage(final Channel channel) {
        final ByteArrayOutputStream builder = channel.attr(DATA).get();
        if (builder == null || builder.size() == 0) {
            return null;
        } else {
            return builder.toByteArray();
        }
    }

    private static int getMessageLength(final Channel channel) {
        final ByteArrayOutputStream builder = channel.attr(DATA).get();
        return (builder == null) ? 0 : builder.size();
    }

    public static void clearMessage(final Channel channel) {
        channel.attr(DATA).set(null);
    }

    private static void appendToMessage(final ChannelHandlerContext ctx, final byte[] bytes) {
        final ByteArrayOutputStream builder = ctx.channel().attr(DATA).get();
        if (builder == null) {
            final ByteArrayOutputStream newBuilder = new ByteArrayOutputStream();
            newBuilder.writeBytes(bytes);
            ctx.channel().attr(DATA).set(newBuilder);
        } else {
            builder.writeBytes(bytes);
        }
    }

    private boolean isMessageFromOurselves(final MimeMessage mimeMessage) {
        if (smtpFrom == null || mimeMessage == null) {
            return false;
        }

        for (InternetAddress fromAddress : MimeUtility.getFromAddresses(mimeMessage)) {
            if (fromAddress.equals(smtpFrom)) {
                LOGGER.warn("Refusing message from: '{}'", fromAddress.getAddress());
                return true;
            }
        }

        return false;
    }

    private void switchToCommandHandler(final ChannelHandlerContext ctx) {
        ctx.pipeline().replace("data-handler", "command-handler", commandHandler);
    }
}
