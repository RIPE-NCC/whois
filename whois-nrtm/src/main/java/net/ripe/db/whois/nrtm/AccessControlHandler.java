package net.ripe.db.whois.nrtm;

import net.ripe.db.whois.common.pipeline.ChannelUtil;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

@Component
@ChannelHandler.Sharable
public class AccessControlHandler extends SimpleChannelUpstreamHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessControlHandler.class);

    private final AccessControlList acl;

    @Autowired
    public AccessControlHandler(final AccessControlList acl) {
        this.acl = acl;
    }

    @Override
    public void channelBound(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        final InetAddress remoteAddress = ChannelUtil.getRemoteAddress(ctx.getChannel());

        if (!acl.isMirror(remoteAddress)) {
            ctx.getChannel().write("%ERROR:402: not authorised to mirror the database from IP address " + remoteAddress.getHostAddress()+"\n").addListener(ChannelFutureListener.CLOSE);
            return;
        }

        ctx.sendUpstream(e);
    }
}
