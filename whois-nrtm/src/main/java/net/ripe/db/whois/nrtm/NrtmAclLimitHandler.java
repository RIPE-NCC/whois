package net.ripe.db.whois.nrtm;

import net.ripe.db.whois.common.pipeline.ChannelUtil;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

/**
 * Handler that immediately closes a channel if the maximum number of open connections is reached for an IP address.
 */
@Component
@ChannelHandler.Sharable
public class NrtmAclLimitHandler extends SimpleChannelUpstreamHandler {

    public static final String REJECTED = "REJECTED";

    private final NrtmLog nrtmLog;
    private final AccessControlListManager accessControlListManager;

    @Autowired
    public NrtmAclLimitHandler(final AccessControlListManager accessControlListManager, final NrtmLog nrtmLog) {
        this.nrtmLog = nrtmLog;
        this.accessControlListManager = accessControlListManager;
    }

    @Override
    public void channelOpen(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        final Channel channel = ctx.getChannel();
        final InetAddress remoteAddress = ChannelUtil.getRemoteAddress(channel);

        if (accessControlListManager.isDenied(remoteAddress)) {
            channel.write(QueryMessages.accessDeniedPermanently(remoteAddress)).addListener(ChannelFutureListener.CLOSE);
            nrtmLog.log(remoteAddress, REJECTED);
            return;
        }

        if (!accessControlListManager.canQueryPersonalObjects(remoteAddress)) {
            channel.write(QueryMessages.accessDeniedTemporarily(remoteAddress)).addListener(ChannelFutureListener.CLOSE);
            nrtmLog.log(remoteAddress, REJECTED);
            return;
        }

        super.channelOpen(ctx, e);
    }

    @Override
    public void channelClosed(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        super.channelClosed(ctx, e);
    }
}
