package net.ripe.db.whois.nrtm;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.ripe.db.whois.common.pipeline.ChannelUtil;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.acl.IpAccessControlListManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

/**
 * Handler that immediately closes a channel if the maximum number of open connections is reached for an IP address.
 */
@Component
@ChannelHandler.Sharable
public class NrtmAclLimitHandler extends ChannelInboundHandlerAdapter {

    public static final String REJECTED = "REJECTED";

    private final NrtmLog nrtmLog;
    private final IpAccessControlListManager IPAccessControlListManager;

    @Autowired
    public NrtmAclLimitHandler(final IpAccessControlListManager IPAccessControlListManager, final NrtmLog nrtmLog) {
        this.nrtmLog = nrtmLog;
        this.IPAccessControlListManager = IPAccessControlListManager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        final Channel channel = ctx.channel();
        final InetAddress remoteAddress = ChannelUtil.getRemoteAddress(channel);

        if (IPAccessControlListManager.isDenied(remoteAddress)) {
            nrtmLog.log(remoteAddress, REJECTED);
            throw new NrtmException(QueryMessages.accessDeniedPermanently(remoteAddress));
        }

        if (!IPAccessControlListManager.canQueryPersonalObjects(remoteAddress)) {
            nrtmLog.log(remoteAddress, REJECTED);
            throw new NrtmException(QueryMessages.accessDeniedTemporarily(remoteAddress));
        }

        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ctx.fireChannelInactive();
    }
}
