package net.ripe.db.whois.smtp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.smtp.SmtpRequest;
import io.netty.util.AttributeKey;
import jakarta.mail.internet.InternetAddress;
import net.ripe.db.whois.common.ApplicationVersion;
import net.ripe.db.whois.smtp.request.DataSmtpRequest;
import net.ripe.db.whois.smtp.request.ExtendedHelloSmtpRequest;
import net.ripe.db.whois.smtp.request.HelloSmtpRequest;
import net.ripe.db.whois.smtp.request.HelpSmtpRequest;
import net.ripe.db.whois.smtp.request.MailSmtpRequest;
import net.ripe.db.whois.smtp.request.NoopSmtpRequest;
import net.ripe.db.whois.smtp.request.QuitSmtpRequest;
import net.ripe.db.whois.smtp.request.RecipientSmtpRequest;
import net.ripe.db.whois.smtp.request.ResetSmtpRequest;
import net.ripe.db.whois.smtp.request.SmtpRequestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@ChannelHandler.Sharable
public class SmtpCommandHandler extends ChannelInboundHandlerAdapter implements SmtpHandler {

    private static final AttributeKey<CharSequence> MAIL_FROM = AttributeKey.newInstance("mail_from");
    private static final AttributeKey<CharSequence> RCPT_TO = AttributeKey.newInstance("rcpt_to");
    private static final AttributeKey<CharSequence> DOMAIN = AttributeKey.newInstance("domain");

    private final SmtpLog smtpLog;
    private final ApplicationVersion applicationVersion;
    private final SmtpDataHandler smtpDataHandler;
    private final int maximumSize;
    private final InternetAddress smtpFrom;

    @Autowired
    public SmtpCommandHandler(
            @Lazy final SmtpDataHandler smtpDataHandler,
            final SmtpLog smtpLog,
            final ApplicationVersion applicationVersion,
            @Value("${mail.smtp.server.maximum.size:0}") final int maximumSize,
            @Value("${mail.smtp.from:}") final String smtpFrom) {
        this.smtpLog = smtpLog;
        this.applicationVersion = applicationVersion;
        this.smtpDataHandler = smtpDataHandler;
        this.maximumSize = maximumSize;
        this.smtpFrom = MimeUtility.parseAddress(smtpFrom);
    }

    @Override
    public void channelRegistered(final ChannelHandlerContext ctx) {
        if (ctx.channel().isActive()) {
            writeResponse(ctx.channel(),  SmtpResponses.banner(applicationVersion.getVersion()));
        }
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        final String command;
        try {
            command = ((ByteBuf) msg).toString(StandardCharsets.US_ASCII).trim();
        } finally {
            ((ByteBuf)msg).release();
        }

        smtpLog.log(ctx.channel(), command);

        final SmtpRequest smtpRequest = new SmtpRequestBuilder(command).build();
        switch (smtpRequest) {
            case HelloSmtpRequest hello -> {
                setDomain(ctx.channel(), hello.parameters().get(0));
                writeResponse(ctx.channel(), SmtpResponses.hello(hello.parameters().get(0)));
            }
            case ExtendedHelloSmtpRequest extendedHello -> {
                setDomain(ctx.channel(), extendedHello.parameters().get(0));
                writeResponse(ctx.channel(), SmtpResponses.extendedHello(extendedHello.parameters().get(0), maximumSize));
            }
            case MailSmtpRequest mail -> {
                if (mail.getFrom() == null || mail.getFrom().isEmpty()) {
                    // An empty From: address is still valid, in particular used for Delivery Status Notifications
                } else {
                    final InternetAddress fromAddress = MimeUtility.parseAddress(mail.getFrom());
                    if (fromAddress == null) {
                        writeResponse(ctx.channel(), SmtpResponses.invalidAddress());
                        break;
                    } else {
                        if ((smtpFrom != null) && (smtpFrom.equals(fromAddress))) {
                            writeResponse(ctx.channel(), SmtpResponses.refusingMessageFrom(smtpFrom.getAddress()));
                            break;
                        }
                    }
                }
                if (maximumSize > 0) {
                    final Integer size = mail.getSize();
                    if ((size != null) && (size > maximumSize)) {
                        writeResponse(ctx.channel(), SmtpResponses.sizeExceeded());
                        break;
                    }
                }
                setMailFrom(ctx.channel(), mail.parameters().get(0));
                writeResponse(ctx.channel(), SmtpResponses.ok());
            }
            case RecipientSmtpRequest recipient -> {
                setRecipient(ctx.channel(), recipient.parameters().get(0));
                writeResponse(ctx.channel(), SmtpResponses.accepted());
            }
            case DataSmtpRequest data -> {
                writeResponse(ctx.channel(), SmtpResponses.enterMessage());
                ctx.pipeline().replace("command-handler", "data-handler", smtpDataHandler);
            }
            case NoopSmtpRequest noop -> writeResponse(ctx.channel(), SmtpResponses.ok());
            case HelpSmtpRequest help -> writeResponse(ctx.channel(), SmtpResponses.help());
            case ResetSmtpRequest reset -> {
                clearMailFrom(ctx.channel());
                clearRecipient(ctx.channel());
                clearDomain(ctx.channel());
                clearData(ctx.channel());
                writeResponse(ctx.channel(), SmtpResponses.ok());
            }
            case QuitSmtpRequest quit -> writeResponseAndClose(ctx.channel(), SmtpResponses.goodbye());
            default -> writeResponse(ctx.channel(), SmtpResponses.unrecognisedCommand());
        }
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        smtpLog.log(ctx.channel(), "(INACTIVE)");
        ctx.fireChannelInactive();
    }

    private CharSequence getMailFrom(final Channel channel) {
        return channel.attr(MAIL_FROM).get();
    }

    private void setMailFrom(final Channel channel, final CharSequence mailFrom) {
        channel.attr(MAIL_FROM).set(mailFrom);
    }

    private void clearMailFrom(final Channel channel) {
        channel.attr(MAIL_FROM).set(null);
    }

    private CharSequence getRecipient(final Channel channel) {
        return channel.attr(RCPT_TO).get();
    }

    private void setRecipient(final Channel channel, final CharSequence recipient) {
        channel.attr(RCPT_TO).set(recipient);
    }

    private void clearRecipient(final Channel channel) {
        channel.attr(RCPT_TO).set(null);
    }

    private CharSequence getDomain(final Channel channel) {
        return channel.attr(DOMAIN).get();
    }

    private void setDomain(final Channel channel, final CharSequence domain) {
        channel.attr(DOMAIN).set(domain);
    }

    private void clearDomain(final Channel channel) {
        channel.attr(DOMAIN).set(null);
    }

    private void clearData(final Channel channel) {
        smtpDataHandler.clearMessage(channel);
    }
}

