package net.ripe.db.whois.query.pipeline;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.query.query.Query;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.springframework.stereotype.Component;

@Component
@ChannelHandler.Sharable
public class QueryDecoder extends OneToOneDecoder {

    @Override
    protected Object decode(final ChannelHandlerContext ctx, final Channel channel, final Object msg) {
        final Query query = Query.parse((String) msg);

        for (final Message warning : query.getWarnings()) {
            channel.write(warning);
        }

        return query;
    }
}
