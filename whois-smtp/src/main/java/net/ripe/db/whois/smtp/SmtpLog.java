package net.ripe.db.whois.smtp;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

@Component
public class SmtpLog {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpLog.class);

    public void log(final ChannelHandlerContext ctx, final InetAddress remoteAddress, final String domainName) {
        LOGGER.info(
                "{} {} {}",
                ctx.channel().id().asShortText(),
                remoteAddress.getHostAddress(),
                domainName != null ? domainName : "NONE"
        );
    }


}
