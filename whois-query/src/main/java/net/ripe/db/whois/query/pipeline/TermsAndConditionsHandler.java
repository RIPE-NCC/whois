package net.ripe.db.whois.query.pipeline;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroupFutureListener;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.query.QueryMessages;
import org.springframework.stereotype.Component;

@Component
@ChannelHandler.Sharable
public class TermsAndConditionsHandler extends ChannelInboundHandlerAdapter {

    private static final Message TERMS_AND_CONDITIONS = QueryMessages.termsAndConditions();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws InterruptedException {
        ChannelFuture channelFuture = ctx.writeAndFlush(TERMS_AND_CONDITIONS);
        channelFuture.addListener(future -> {
            if (!future.isSuccess()) {
                System.out.println("writing failed: " + future.cause());
            } else {
                System.out.println("write succeeded");
            }
        });
//        if (!channelFuture.isSuccess()) {
//            System.out.println("writing failed: " + channelFuture.cause());
//        } else {
//            System.out.println("writing succeeded");
//        }
//        ctx.pipeline().remove(this);
        ctx.fireChannelActive();
    }

}
