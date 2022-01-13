package net.ripe.db.whois.nrtm;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.ripe.db.whois.common.pipeline.ChannelUtil;
import net.ripe.db.whois.common.pipeline.ConnectionCounter;
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
public class NrtmConnectionPerIpLimitHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmConnectionPerIpLimitHandler.class);

    private final ConnectionCounter connectionCounter;
    private final int maxConnectionsPerIp;
    private final NrtmLog nrtmLog;

    @Autowired
    public NrtmConnectionPerIpLimitHandler(
            @Value("${whois.limit.connectionsPerIp:3}") final int maxConnectionsPerIp,
            final NrtmLog nrtmLog) {
        this.maxConnectionsPerIp = maxConnectionsPerIp;
        this.nrtmLog = nrtmLog;
        this.connectionCounter = new ConnectionCounter();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final Channel channel = ctx.channel();
        final InetAddress remoteAddress = ChannelUtil.getRemoteAddress(channel);

        if (limitConnections(remoteAddress) && connectionsExceeded(remoteAddress)) {
            nrtmLog.log(remoteAddress, "REJECTED");
            channel.write(NrtmMessages.connectionsExceeded(maxConnectionsPerIp)).addListener(ChannelFutureListener.CLOSE);
            return;
        }

        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final Channel channel = ctx.channel();
        final InetAddress remoteAddress = ChannelUtil.getRemoteAddress(channel);
        connectionCounter.decrement(remoteAddress);

        ctx.fireChannelInactive();
    }


    private boolean limitConnections(final InetAddress remoteAddress) {
        // unlike query port, no exceptions made for internal addresses
        return maxConnectionsPerIp > 0;
    }

    private boolean connectionsExceeded(final InetAddress remoteAddresss) {
        final Integer count = connectionCounter.increment(remoteAddresss);
        return (count != null && count > maxConnectionsPerIp);
    }
}
