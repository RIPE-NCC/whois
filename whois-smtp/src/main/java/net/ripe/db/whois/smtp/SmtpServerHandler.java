package net.ripe.db.whois.smtp;

import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import net.ripe.db.whois.api.mail.dao.MailMessageDao;
import net.ripe.db.whois.common.ApplicationVersion;
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

import java.util.concurrent.ScheduledFuture;

public class SmtpServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpServerHandler.class);

    private static final AttributeKey<String> MAIL_FROM = AttributeKey.newInstance("mail_from");

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
        if (ctx.channel().isActive()) {
            writeMessage(ctx.channel(),  SmtpMessages.banner(applicationVersion.getVersion()));
        }
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        try {
            // TODO: log all connections even if DATA is not sent
            final SmtpCommand smtpCommand = SmtpCommandBuilder.build(msg.toString().trim());
            switch (smtpCommand) {
                case HelloCommand helloCommand -> {
                    // TODO: log remote name and address
                    writeMessage(ctx.channel(), SmtpMessages.hello(helloCommand.getValue()));
                }
                case ExtendedHelloCommand extendedHelloCommand -> {
                    // TODO: log remote name and address
                    writeMessage(ctx.channel(), SmtpMessages.extendedHello(extendedHelloCommand.getValue()));
                }
                case MailCommand mailCommand -> {
                    // TODO: log mail from
                    LOGGER.info("Mail From: {} {}", ctx.channel().id(), mailCommand.getValue());
                    ctx.channel().attr(MAIL_FROM).set(mailCommand.getValue());
                    writeMessage(ctx.channel(), SmtpMessages.ok());
                }
                case RecipientCommand recipientCommand -> {
                    // TODO: log recipient
                    writeMessage(ctx.channel(), SmtpMessages.accepted());
                }
                case DataCommand dataCommand -> {
                    // TODO: process DATA
                    writeMessage(ctx.channel(), SmtpMessages.enterMessage());
                }
                case NoopCommand noopCommand -> writeMessage(ctx.channel(), SmtpMessages.ok());
                case HelpCommand helpCommand -> writeMessage(ctx.channel(), SmtpMessages.help());
                case ResetCommand resetCommand -> {
                    // TODO: discard all accepted data
                    writeMessage(ctx.channel(), SmtpMessages.ok());
                }
                case QuitCommand quitCommand -> {
                    // TODO: log remote address, addresses, message size etc to mail log
                    // TODO: log data message to separate raw log
                    LOGGER.info("Quit: {} {}", ctx.channel().id(), ctx.channel().attr(MAIL_FROM).get());
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
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }

        ctx.fireChannelInactive();
    }

    private void writeMessage(final Channel channel, final Object message) {
        if (!channel.isOpen()) {
            throw new ChannelException();
        }

        channel.writeAndFlush(message + "\n");
    }

    private void writeMessageAndClose(final Channel channel, final Object message) {
        if (!channel.isOpen()) {
            throw new ChannelException();
        }

        channel.writeAndFlush(message + "\n").addListener(ChannelFutureListener.CLOSE);
    }
}

