package net.ripe.db.whois.query.pipeline;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.QueryParser;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
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

    private static final Charset DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;

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
            return DEFAULT_CHARSET.name();
        }

        final QueryParser queryParser = new QueryParser(query.toString());
        final String queryCharset = queryParser.getOptionValue(QueryFlag.CHARSET);

        try {
            return getCharsetForName(queryCharset).name();
        } catch (UnsupportedCharsetException ex){
            throw new QueryException(QueryCompletionInfo.PARAMETER_ERROR, QueryMessages.invalidCharsetPassed(queryCharset));
        }
    }

    private static Charset getCharsetForName(final String charsetName) {
        if ("latin-1".equalsIgnoreCase(charsetName)) {
            return DEFAULT_CHARSET;
        }
        return Charset.forName(charsetName);
    }
}
