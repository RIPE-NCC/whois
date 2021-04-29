package net.ripe.db.whois.query.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import net.ripe.db.whois.common.ApplicationVersion;
import net.ripe.db.whois.common.pipeline.MaintenanceHandler;
import net.ripe.db.whois.query.handler.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Component
public class WhoisServerChannelInitializer extends ChannelInitializer<Channel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisServerChannelInitializer.class);

    private static final ByteBuf LINE_DELIMITER = Unpooled.wrappedBuffer(new byte[]{'\n'});

    // TODO: [ES] interrupt doesn't work in keepalive mode
    private static final ByteBuf INTERRUPT_DELIMITER = Unpooled.wrappedBuffer(new byte[]{(byte)0xff, (byte)0xf4, (byte)0xff, (byte)0xfd, (byte)0x6});

    @Value("${whois.read.timeout.sec:180}")
    private int TIMEOUT_SECONDS;

    private static final int POOL_SIZE = 64;

    private final StringDecoder stringDecoder = new StringDecoder(StandardCharsets.UTF_8);
    private final EventExecutorGroup executorGroup = new DefaultEventExecutorGroup(POOL_SIZE);

    private final MaintenanceHandler maintenanceHandler;
    private final ConnectionPerIpLimitHandler connectionPerIpLimitHandler;
    private final QueryChannelsRegistry queryChannelsRegistry;
    private final TermsAndConditionsHandler termsAndConditionsHandler;
    private final WhoisEncoder whoisEncoder;
    private final QueryDecoder queryDecoder;
    private final QueryHandler queryHandler;
    private final ApplicationVersion applicationVersion;
    private final boolean proxyProtocolEnabled;

    @Autowired
    public WhoisServerChannelInitializer(final MaintenanceHandler maintenanceHandler,
                                         final QueryChannelsRegistry queryChannelsRegistry,
                                         final TermsAndConditionsHandler termsAndConditionsHandler,
                                         final QueryDecoder queryDecoder,
                                         final WhoisEncoder whoisEncoder,
                                         final ConnectionPerIpLimitHandler connectionPerIpLimitHandler,
                                         final QueryHandler queryHandler,
                                         final ApplicationVersion applicationVersion,
                                         final @Value("${proxy.protocol.enabled:false}") boolean proxyProtocolEnabled) {
        this.maintenanceHandler = maintenanceHandler;
        this.queryChannelsRegistry = queryChannelsRegistry;
        this.termsAndConditionsHandler = termsAndConditionsHandler;
        this.queryDecoder = queryDecoder;
        this.whoisEncoder = whoisEncoder;
        this.connectionPerIpLimitHandler = connectionPerIpLimitHandler;
        this.queryHandler = queryHandler;
        this.applicationVersion = applicationVersion;
        this.proxyProtocolEnabled = proxyProtocolEnabled;

        if (proxyProtocolEnabled) {
            LOGGER.info("Proxy protocol handler enabled");
        }
    }

    @Override
    public void initChannel(Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();

        pipeline.addLast("maintenanceHandler", maintenanceHandler);
        pipeline.addLast("connectionPerIpLimit", connectionPerIpLimitHandler);

        pipeline.addLast("query-channels", queryChannelsRegistry);
        pipeline.addLast("read-timeout", new KeepChannelOpenOnReadTimeoutHandler(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        pipeline.addLast("write-timeout", new WriteTimeoutHandler(TIMEOUT_SECONDS, TimeUnit.SECONDS));

        pipeline.addLast("terms-conditions", termsAndConditionsHandler);

        if (proxyProtocolEnabled) {
            pipeline.addLast(ProxyProtocolChannelHandler.NAME, new ProxyProtocolChannelHandler());
        }

        pipeline.addLast("delimiter", new DelimiterBasedFrameDecoder(1024, LINE_DELIMITER, INTERRUPT_DELIMITER));

        pipeline.addLast("string-decoder", stringDecoder);
        pipeline.addLast(executorGroup, "whois-encoder", whoisEncoder);

        pipeline.addLast(executorGroup, "query-decoder", queryDecoder);
        pipeline.addLast(executorGroup, "connection-state", new ConnectionStateHandler());

        pipeline.addLast(executorGroup, "served-by", new ServedByHandler(applicationVersion.getVersion()));
        pipeline.addLast(executorGroup, "whois", new WhoisServerHandler(queryHandler));
        pipeline.addLast("exception", new ExceptionHandler());
    }

}
