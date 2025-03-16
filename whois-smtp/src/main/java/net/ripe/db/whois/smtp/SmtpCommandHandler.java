package net.ripe.db.whois.smtp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import net.ripe.db.whois.common.ApplicationVersion;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.pipeline.ChannelUtil;
import net.ripe.db.whois.smtp.commands.DataCommand;
import net.ripe.db.whois.smtp.commands.ExtendedHelloCommand;
import net.ripe.db.whois.smtp.commands.HelloCommand;
import net.ripe.db.whois.smtp.commands.HelpCommand;
import net.ripe.db.whois.smtp.commands.MailCommand;
import net.ripe.db.whois.smtp.commands.NoopCommand;
import net.ripe.db.whois.smtp.commands.QuitCommand;
import net.ripe.db.whois.smtp.commands.RecipientCommand;
import net.ripe.db.whois.smtp.commands.ResetCommand;
import net.ripe.db.whois.smtp.commands.SmtpCommand;
import net.ripe.db.whois.smtp.commands.SmtpCommandBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

@Component
@ChannelHandler.Sharable
public class SmtpCommandHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpCommandHandler.class);

    private static final AttributeKey<String> MAIL_FROM = AttributeKey.newInstance("mail_from");
    private static final AttributeKey<String> RCPT_TO = AttributeKey.newInstance("rcpt_to");
    private static final AttributeKey<String> DOMAIN = AttributeKey.newInstance("domain");

    private final SmtpLog smtpLog;
    private final ApplicationVersion applicationVersion;
    private final SmtpDataHandler smtpDataHandler;

    @Autowired
    public SmtpCommandHandler(
            @Lazy final SmtpDataHandler smtpDataHandler,
            final SmtpLog smtpLog,
            final ApplicationVersion applicationVersion) {
        this.smtpLog = smtpLog;
        this.applicationVersion = applicationVersion;
        this.smtpDataHandler = smtpDataHandler;
    }

    @Override
    public void channelRegistered(final ChannelHandlerContext ctx) {
        if (ctx.channel().isActive()) {
            writeMessage(ctx.channel(),  SmtpMessages.banner(applicationVersion.getVersion()));
        }
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        final String command = ((ByteBuf) msg).toString(StandardCharsets.US_ASCII).trim();
        smtpLog.log("Channel {}: {}", ctx.channel().id(), command);
        try {
            final SmtpCommand smtpCommand = SmtpCommandBuilder.build(command);
            switch (smtpCommand) {
                case HelloCommand helloCommand -> {
                    setDomain(ctx.channel(), helloCommand.getValue());
                    writeMessage(ctx.channel(), SmtpMessages.hello(helloCommand.getValue()));
                }
                case ExtendedHelloCommand extendedHelloCommand -> {
                    setDomain(ctx.channel(), extendedHelloCommand.getValue());
                    writeMessage(ctx.channel(), SmtpMessages.extendedHello(extendedHelloCommand.getValue()));
                }
                case MailCommand mailCommand -> {
                    setMailFrom(ctx.channel(), mailCommand.getValue());
                    writeMessage(ctx.channel(), SmtpMessages.ok());
                }
                case RecipientCommand recipientCommand -> {
                    setRecipient(ctx.channel(), recipientCommand.getValue());
                    writeMessage(ctx.channel(), SmtpMessages.accepted());
                }
                case DataCommand dataCommand -> {
                    writeMessage(ctx.channel(), SmtpMessages.enterMessage());
                    ctx.pipeline().replace("U-command-handler", "U-data-handler", smtpDataHandler);
                }
                case NoopCommand noopCommand -> writeMessage(ctx.channel(), SmtpMessages.ok());
                case HelpCommand helpCommand -> writeMessage(ctx.channel(), SmtpMessages.help());
                case ResetCommand resetCommand -> {
                    clearMailFrom(ctx.channel());
                    clearRecipient(ctx.channel());
                    clearDomain(ctx.channel());
                    clearData(ctx.channel());
                    writeMessage(ctx.channel(), SmtpMessages.ok());
                }
                case QuitCommand quitCommand -> {
                    writeMessageAndClose(ctx.channel(), SmtpMessages.goodbye());
                }
                default -> writeMessage(ctx.channel(), SmtpMessages.unrecognisedCommand());
            }
        } catch (IllegalArgumentException e) {
            writeMessage(ctx.channel(), SmtpMessages.unrecognisedCommand());
        }
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        final String domain = getDomain(ctx.channel());
        final InetAddress remoteAddress = ChannelUtil.getRemoteAddress(ctx.channel());
        smtpLog.log(ctx, remoteAddress, domain);
        ctx.fireChannelInactive();
    }

    private void writeMessage(final Channel channel, final Message message) {
        if (!channel.isOpen()) {
            throw new ChannelException();
        }

        channel.writeAndFlush(message);
    }

    private void writeMessageAndClose(final Channel channel, final Message message) {
        if (!channel.isOpen()) {
            throw new ChannelException();
        }

        channel.writeAndFlush(message).addListener(ChannelFutureListener.CLOSE);
    }


    private String getMailFrom(final Channel channel) {
        return channel.attr(MAIL_FROM).get();
    }

    private void setMailFrom(final Channel channel, final String mailFrom) {
        channel.attr(MAIL_FROM).set(mailFrom);
    }

    private void clearMailFrom(final Channel channel) {
        channel.attr(MAIL_FROM).set(null);
    }

    private String getRecipient(final Channel channel) {
        return channel.attr(RCPT_TO).get();
    }

    private void setRecipient(final Channel channel, final String recipient) {
        channel.attr(RCPT_TO).set(recipient);
    }

    private void clearRecipient(final Channel channel) {
        channel.attr(RCPT_TO).set(null);
    }

    private String getDomain(final Channel channel) {
        return channel.attr(DOMAIN).get();
    }

    private void setDomain(final Channel channel, final String domain) {
        channel.attr(DOMAIN).set(domain);
    }

    private void clearDomain(final Channel channel) {
        channel.attr(DOMAIN).set(null);
    }

    private byte[] getData(final Channel channel) {
        return smtpDataHandler.getData(channel);
    }

    private void clearData(final Channel channel) {
        smtpDataHandler.clearData(channel);
    }
}

