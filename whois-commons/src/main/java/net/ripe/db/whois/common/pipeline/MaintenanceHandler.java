package net.ripe.db.whois.common.pipeline;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import net.ripe.db.whois.common.MaintenanceMode;
import net.ripe.db.whois.common.ip.IpInterval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

/**
 * close the channel when in maintenance mode
 */
@Component
@ChannelHandler.Sharable
public class MaintenanceHandler extends ChannelInboundHandlerAdapter {
    private final AttributeKey<Object> CONNECTION_REFUSED_KEY = AttributeKey.newInstance("connection_refused");
    private static final Object CONNECTION_REFUSED = new Object();

    private final MaintenanceMode maintenanceMode;

    @Autowired
    public MaintenanceHandler(final MaintenanceMode maintenanceMode) {
        this.maintenanceMode = maintenanceMode;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        final Channel channel = ctx.channel();
        final InetAddress remoteAddress = ChannelUtil.getRemoteAddress(channel);
        final IpInterval remoteIp = IpInterval.asIpInterval(remoteAddress);

        if (!maintenanceMode.allowRead(remoteIp)) {
            channel.attr(CONNECTION_REFUSED_KEY).set(CONNECTION_REFUSED);
            channel.close();
            return;
        }

        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {

        if (ctx.channel().attr(CONNECTION_REFUSED_KEY).get() == CONNECTION_REFUSED) {
            return;
        }

        ctx.fireChannelInactive();
    }
}
