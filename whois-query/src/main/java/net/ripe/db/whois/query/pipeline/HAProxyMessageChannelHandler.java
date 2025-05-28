package net.ripe.db.whois.query.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import net.ripe.db.whois.common.pipeline.ChannelUtil;

import java.net.InetAddress;

public class HAProxyMessageChannelHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HAProxyMessage) {
            HAProxyMessage hapm = (HAProxyMessage) msg;
            try {
                String sourceAddress = hapm.sourceAddress();
                if (sourceAddress != null) {
                    switch (hapm.proxiedProtocol()) {
                        case TCP4:
                        case TCP6:
                            ctx.channel().attr(ChannelUtil.CLIENT_IP).set(InetAddress.getByName(hapm.sourceAddress()));
                            break;
                        default:
                            throw new UnsupportedOperationException("unsupported proxy protocol " + hapm.proxiedProtocol());
                    }
                }

                ctx.pipeline().remove(this);
                ctx.fireChannelActive(); // trigger channel active again now client ip has been set
            } finally {
                hapm.release();
            }
        }
    }
}
