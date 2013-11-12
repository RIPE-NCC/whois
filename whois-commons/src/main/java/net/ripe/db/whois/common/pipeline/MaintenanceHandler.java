package net.ripe.db.whois.common.pipeline;

import net.ripe.db.whois.common.MaintenanceMode;
import net.ripe.db.whois.common.ip.IpInterval;
import org.jboss.netty.channel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

/**
 * close the channel when in maintenance mode
 */
@Component
@ChannelHandler.Sharable
public class MaintenanceHandler extends SimpleChannelUpstreamHandler {
    private static final Object CONNECTION_REFUSED = new Object();

    private final MaintenanceMode maintenanceMode;

    @Autowired
    public MaintenanceHandler(final MaintenanceMode maintenanceMode) {
        this.maintenanceMode = maintenanceMode;
    }

    @Override
    public void channelOpen(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        final Channel channel = ctx.getChannel();
        final InetAddress remoteAddress = ChannelUtil.getRemoteAddress(channel);
        final IpInterval remoteIp = IpInterval.asIpInterval(remoteAddress);

        if (!maintenanceMode.allowRead(remoteIp)) {
            ctx.setAttachment(CONNECTION_REFUSED);
            channel.close();
            return;
        }

        super.channelOpen(ctx, e);
    }

    @Override
    public void channelClosed(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        if (ctx.getAttachment() == CONNECTION_REFUSED) {
            return;
        }

        super.channelClosed(ctx, e);
    }
}
