package net.ripe.db.whois.query.pipeline;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroupFutureListener;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.query.QueryMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static net.ripe.db.whois.query.pipeline.ConnectionStateHandler.NEWLINE;

@Component
@ChannelHandler.Sharable
public class TermsAndConditionsHandler extends ChannelInboundHandlerAdapter {
    private static final Message TERMS_AND_CONDITIONS = QueryMessages.termsAndConditions();

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.write(TERMS_AND_CONDITIONS);
        ctx.writeAndFlush(NEWLINE);
        ctx.pipeline().remove(this);
        ctx.fireChannelActive();
    }

}
