package net.ripe.db.whois.query.pipeline;

import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.pipeline.ChannelUtil;
import net.ripe.db.whois.query.acl.IpResourceConfiguration;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryMessages;
import net.ripe.db.whois.query.handler.WhoisLog;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handler that immediately closes a channel if the maximum number of open connections is reached for an IP address.
 */
@Component
@ChannelHandler.Sharable
public class ConnectionPerIpLimitHandler extends SimpleChannelUpstreamHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPerIpLimitHandler.class);
    private static final Integer INTEGER_ONE = Integer.valueOf(1);

    private final IpResourceConfiguration ipResourceConfiguration;
    private final WhoisLog whoisLog;
    private final ConcurrentHashMap<InetAddress, Integer> connections = new ConcurrentHashMap<>();

    @Value("${whois.limit.connectionsPerIp:3}") private volatile int maxConnectionsPerIp;
    @Value("${application.version}") private volatile String version;

    @Autowired
    public ConnectionPerIpLimitHandler(final IpResourceConfiguration ipResourceConfiguration, final WhoisLog whoisLog) {
        this.ipResourceConfiguration = ipResourceConfiguration;
        this.whoisLog = whoisLog;
    }

    void setMaxConnectionsPerIp(final int maxConnectionsPerIp) {
        this.maxConnectionsPerIp = maxConnectionsPerIp;
    }

    @Override
    public void channelOpen(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        final Channel channel = ctx.getChannel();
        final InetAddress remoteAddress = ChannelUtil.getRemoteAddress(channel);
        final IpInterval remoteIp = IpInterval.asIpInterval(remoteAddress);
        Integer count = incrementOrCreate(remoteAddress);

        if (limitConnections(remoteIp)) {
            if (count != null && count >= maxConnectionsPerIp) {
                whoisLog.logQueryResult("QRY", 0, 0, QueryCompletionInfo.REJECTED, 0, remoteAddress, channel.getId(), "");
                channel.write(QueryMessages.termsAndConditions());
                channel.write(QueryMessages.connectionsExceeded(maxConnectionsPerIp));
                channel.write(QueryMessages.servedByNotice(version)).addListener(ChannelFutureListener.CLOSE);
                return;
            }
        }

        super.channelOpen(ctx, e);
    }

    @Override
    public void channelClosed(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        final Channel channel = ctx.getChannel();
        final InetAddress remoteAddress = ChannelUtil.getRemoteAddress(channel);
        decrementOrDrop(remoteAddress);

        super.channelClosed(ctx, e);
    }

    private boolean limitConnections(final IpInterval remoteAddress) {
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

    private Integer incrementOrCreate(InetAddress remoteAddress) {
        Integer count;
        do {
            count = connections.putIfAbsent(remoteAddress, INTEGER_ONE);
        } while (count != null && !connections.replace(remoteAddress, count, count + 1));
        return count;
    }

    private void decrementOrDrop(InetAddress remoteAddress) {
        Integer count;
        for (; ; ) {
            count = connections.get(remoteAddress);

            if (count == INTEGER_ONE) {
                if (connections.remove(remoteAddress, INTEGER_ONE)) {
                    break;
                }
            } else if (count == null) {
                break;
            } else if (connections.replace(remoteAddress, count, count - 1)) {
                break;
            }
        }
    }

}
