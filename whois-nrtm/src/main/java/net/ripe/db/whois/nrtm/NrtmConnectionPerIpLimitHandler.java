package net.ripe.db.whois.nrtm;

import net.ripe.db.whois.common.pipeline.ChannelUtil;
import net.ripe.db.whois.common.pipeline.ConnectionCounter;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

/**
 * Handler that immediately closes a channel if the maximum number of open connections is reached for an IP address.
 */
@Component
@ChannelHandler.Sharable
public class NrtmConnectionPerIpLimitHandler extends SimpleChannelUpstreamHandler {

    public static final String REJECTED = "REJECTED";

    private final ConnectionCounter connectionCounter;
    private final int maxConnectionsPerIp;
    private final NrtmLog nrtmLog;
    private final AccessControlListManager accessControlListManager;

    @Autowired
    public NrtmConnectionPerIpLimitHandler(
            @Value("${whois.limit.connectionsPerIp:3}") final int maxConnectionsPerIp,
            final AccessControlListManager accessControlListManager,
            final NrtmLog nrtmLog) {
        this.maxConnectionsPerIp = maxConnectionsPerIp;
        this.nrtmLog = nrtmLog;
        this.connectionCounter = new ConnectionCounter();
        this.accessControlListManager = accessControlListManager;
    }

    @Override
    public void channelOpen(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        final Channel channel = ctx.getChannel();
        final InetAddress remoteAddress = ChannelUtil.getRemoteAddress(channel);

        if(!canOpenConnection(channel, remoteAddress)) {
            nrtmLog.log(remoteAddress, REJECTED);
            return;
        }

        super.channelOpen(ctx, e);
    }

    @Override
    public void channelClosed(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        final Channel channel = ctx.getChannel();
        final InetAddress remoteAddress = ChannelUtil.getRemoteAddress(channel);
        connectionCounter.decrement(remoteAddress);

        super.channelClosed(ctx, e);
    }

    private boolean limitConnections(final InetAddress remoteAddress) {
        // unlike query port, no exceptions made for internal addresses
        return maxConnectionsPerIp > 0;
    }

    private boolean connectionsExceeded(final InetAddress remoteAddresss) {
        final Integer count = connectionCounter.increment(remoteAddresss);
        return (count != null && count > maxConnectionsPerIp);
    }

    private boolean canOpenConnection(final Channel channel, final InetAddress remoteAddress) {

        if (accessControlListManager.isDenied(remoteAddress)) {
            channel.write(QueryMessages.accessDeniedPermanently(remoteAddress)).addListener(ChannelFutureListener.CLOSE);
            return false;
        }

        if (!accessControlListManager.canQueryPersonalObjects(remoteAddress)) {
            channel.write(QueryMessages.accessDeniedTemporarily(remoteAddress)).addListener(ChannelFutureListener.CLOSE);
            return false;
        }

        if (limitConnections(remoteAddress) && connectionsExceeded(remoteAddress)) {
            channel.write(NrtmMessages.connectionsExceeded(maxConnectionsPerIp)).addListener(ChannelFutureListener.CLOSE);
            return false;
        }
        return true;
    }
}
