package net.ripe.db.whois.query.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.ProtocolDetectionState;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import org.springframework.stereotype.Component;

@Component
@ChannelHandler.Sharable
public class ProxyProtocolChannelHandler extends ChannelInboundHandlerAdapter {

    public static final String NAME = ProxyProtocolChannelHandler.class.getSimpleName();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (isHAPMDetected(msg)) {
            ctx.pipeline().addAfter(NAME, null, new HAProxyMessageChannelHandler())
                    .replace(this, null, new HAProxyMessageDecoder());
        } else {
            ctx.pipeline().remove(this);
        }
        super.channelRead(ctx, msg);
    }

    private boolean isHAPMDetected(Object msg) {
        return HAProxyMessageDecoder.detectProtocol((ByteBuf) msg).state() == ProtocolDetectionState.DETECTED;
    }

}
