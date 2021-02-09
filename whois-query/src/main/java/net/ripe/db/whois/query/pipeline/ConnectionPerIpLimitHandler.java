package net.ripe.db.whois.query.pipeline;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.ripe.db.whois.common.ApplicationVersion;
import net.ripe.db.whois.common.pipeline.ChannelUtil;
import net.ripe.db.whois.common.pipeline.ConnectionCounter;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.acl.IpResourceConfiguration;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.handler.WhoisLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

/**
 * Handler that immediately closes a channel if the maximum number of open connections is reached for an IP address.
 */
@Component
@ChannelHandler.Sharable
public class ConnectionPerIpLimitHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPerIpLimitHandler.class);

    private final IpResourceConfiguration ipResourceConfiguration;
    private final WhoisLog whoisLog;
    private final ConnectionCounter connectionCounter;
    private final int maxConnectionsPerIp;
    private final ApplicationVersion applicationVersion;

    @Autowired
    public ConnectionPerIpLimitHandler(
            final IpResourceConfiguration ipResourceConfiguration,
            final WhoisLog whoisLog,
            @Value("${whois.limit.connectionsPerIp:3}") final int maxConnectionsPerIp,
            final ApplicationVersion applicationVersion) {
        this.ipResourceConfiguration = ipResourceConfiguration;
        this.whoisLog = whoisLog;
        this.maxConnectionsPerIp = maxConnectionsPerIp;
        this.applicationVersion = applicationVersion;
        this.connectionCounter = new ConnectionCounter();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        final Channel channel = ctx.channel();
        final InetAddress remoteAddress = ChannelUtil.getRemoteAddress(channel);

        if (limitConnections(remoteAddress) && connectionsExceeded(remoteAddress)) {
            whoisLog.logQueryResult("QRY", 0, 0, QueryCompletionInfo.REJECTED, 0, remoteAddress, channel.id().asLongText(), "");
            channel.write(QueryMessages.termsAndConditions());
            channel.write(QueryMessages.connectionsExceeded(maxConnectionsPerIp));
            channel.write(QueryMessages.servedByNotice(applicationVersion.getVersion())).addListener(ChannelFutureListener.CLOSE);
            return;
        }

        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        final Channel channel = ctx.channel();
        final InetAddress remoteAddress = ChannelUtil.getRemoteAddress(channel);
        connectionCounter.decrement(remoteAddress);
        ctx.fireChannelInactive();
    }

    private boolean limitConnections(final InetAddress remoteAddress) {
        if (ipResourceConfiguration.isUnlimitedConnections(remoteAddress)) {
            LOGGER.debug("Unlimited connections allowed for {}", remoteAddress);
            return false;
        }

        if (ipResourceConfiguration.isProxy(remoteAddress)) {
            LOGGER.debug("Unlimited connections allowed for client with proxy {}", remoteAddress);
            return false;
        }

        return maxConnectionsPerIp > 0;
    }

    private boolean connectionsExceeded(final InetAddress remoteAddress) {
        final Integer count = connectionCounter.increment(remoteAddress);
        return (count != null && count >= maxConnectionsPerIp);
    }
}
