package net.ripe.db.whois.smtp;

import io.netty.channel.Channel;
import net.ripe.db.whois.common.pipeline.ChannelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SmtpLog {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpLog.class);

    public void log(final Channel channel, final String command) {
        LOGGER.info(
                "{} {} {}",
                channel.id(),
                ChannelUtil.getRemoteAddress(channel),
                command
        );
    }

}
