package net.ripe.db.whois.nrtm;

import net.ripe.db.whois.common.pipeline.ChannelUtil;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

@Component
@ChannelHandler.Sharable
public class AccessControlHandler extends SimpleChannelUpstreamHandler {

    private final AccessControlList acl;

    @Autowired
    public AccessControlHandler(final AccessControlList acl) {
        this.acl = acl;
    }

    @Override
    public void channelBound(final ChannelHandlerContext ctx, final ChannelStateEvent e) {
        final InetAddress remoteAddress = ChannelUtil.getRemoteAddress(ctx.getChannel());

        if (!acl.isMirror(remoteAddress)) {
            ctx.getChannel().write(NrtmMessages.notAuthorised(remoteAddress.getHostAddress())).addListener(ChannelFutureListener.CLOSE);
            return;
        }

        ctx.sendUpstream(e);
    }
}
