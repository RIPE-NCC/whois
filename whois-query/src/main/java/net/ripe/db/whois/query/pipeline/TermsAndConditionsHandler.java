package net.ripe.db.whois.query.pipeline;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.query.QueryMessages;
import org.springframework.stereotype.Component;

@Component
@ChannelHandler.Sharable
public class TermsAndConditionsHandler extends ChannelInboundHandlerAdapter {
    private static final Message TERMS_AND_CONDITIONS = QueryMessages.termsAndConditions();

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.channel().write(TERMS_AND_CONDITIONS);
        ctx.pipeline().remove(this);
        ctx.fireChannelActive();
    }
}
