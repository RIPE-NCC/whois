package net.ripe.db.whois.query.pipeline;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.query.domain.QueryMessages;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.springframework.stereotype.Component;

@Component
@ChannelHandler.Sharable
public class TermsAndConditionsHandler extends SimpleChannelUpstreamHandler {
    private static final Message TERMS_AND_CONDITIONS = QueryMessages.termsAndConditions();

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        e.getChannel().write(TERMS_AND_CONDITIONS);

        ctx.sendUpstream(e);
    }
}
