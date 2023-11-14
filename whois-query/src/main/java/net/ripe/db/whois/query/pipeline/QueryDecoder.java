package net.ripe.db.whois.query.pipeline;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.query.acl.IpAccessControlListManager;
import net.ripe.db.whois.query.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.List;

@Component
@ChannelHandler.Sharable
public class QueryDecoder extends MessageToMessageDecoder<String> {

    private final IpAccessControlListManager IPAccessControlListManager;

    @Autowired
    public QueryDecoder(final IpAccessControlListManager IPAccessControlListManager) {
        this.IPAccessControlListManager = IPAccessControlListManager;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, String msg, List<Object> list) {
        Channel channel = ctx.channel();
        final Query query = Query.parse(msg, Query.Origin.LEGACY, isTrusted(channel));

        for (final Message warning : query.getWarnings()) {
            channel.write(warning);
        }

        list.add(query);
    }

    private boolean isTrusted(final Channel channel) {
        return IPAccessControlListManager.isTrusted(((InetSocketAddress)channel.remoteAddress()).getAddress());
    }
}
