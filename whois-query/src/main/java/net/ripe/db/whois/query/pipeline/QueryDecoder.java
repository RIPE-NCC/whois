package net.ripe.db.whois.query.pipeline;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.QueryParser;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;

import static net.ripe.db.whois.query.pipeline.WhoisEncoder.CHARSET_ATTRIBUTE;

@Component
@ChannelHandler.Sharable
public class QueryDecoder extends MessageToMessageDecoder<String> {

    private final AccessControlListManager accessControlListManager;

    @Autowired
    public QueryDecoder(final AccessControlListManager accessControlListManager) {
        this.accessControlListManager = accessControlListManager;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, String msg, List<Object> list) {
        Channel channel = ctx.channel();
        final Query query = Query.parse(msg, Query.Origin.LEGACY, isTrusted(channel));
        ctx.channel().attr(CHARSET_ATTRIBUTE).set(getCharsetName(query));

        for (final Message warning : query.getWarnings()) {
            channel.write(warning);
        }

        list.add(query);
    }

    private boolean isTrusted(final Channel channel) {
        return accessControlListManager.isTrusted(((InetSocketAddress)channel.remoteAddress()).getAddress());
    }

    private String getCharsetName(final Query query){
        if (!query.isCharsetSpecified()){
            return StandardCharsets.ISO_8859_1.name();
        }

        final QueryParser queryParser = new QueryParser(query.toString());
        final String queryCharset = queryParser.getOptionValue(QueryFlag.CHARSET);

        try {
            return Charset.forName(queryCharset).name();
        } catch (UnsupportedCharsetException ex){
            if ("latin-1".equalsIgnoreCase(queryCharset)){
                return StandardCharsets.ISO_8859_1.name();
            }
            throw new IllegalArgumentException("Unsupported charset", ex);
        }
    }
}
