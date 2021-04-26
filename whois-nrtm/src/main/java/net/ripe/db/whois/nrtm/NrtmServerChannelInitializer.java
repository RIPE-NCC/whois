package net.ripe.db.whois.nrtm;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import net.ripe.db.whois.common.pipeline.MaintenanceHandler;
import net.ripe.db.whois.query.pipeline.ProxyProtocolChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class NrtmServerChannelInitializer extends ChannelInitializer<Channel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmServerChannelInitializer.class);

    private static final ByteBuf LINE_DELIMITER = Unpooled.wrappedBuffer(new byte[]{'\n'});
    private static final int DELIMITER_MAX_FRAME_LENGTH = 128;
    private static final boolean STRIP_DELIMITER = true;

    private static final int POOL_SIZE = 32;

    private final StringDecoder stringDecoder = new StringDecoder(StandardCharsets.UTF_8);
    private final StringEncoder stringEncoder = new StringEncoder(StandardCharsets.UTF_8);
    private final EventExecutorGroup executorGroup = new DefaultEventExecutorGroup(POOL_SIZE);

    private final NrtmChannelsRegistry nrtmChannelsRegistry;
    private final NrtmExceptionHandler exceptionHandler;
    private final MaintenanceHandler maintenanceHandler;
    private final NrtmQueryHandlerFactory nrtmQueryHandlerFactory;
    private final NrtmConnectionPerIpLimitHandler nrtmConnectionPerIpLimitHandler;
    private final NrtmAclLimitHandler nrtmAclLimitHandler;
    private final boolean proxyProtocolEnabled;

    protected NrtmServerChannelInitializer(final NrtmChannelsRegistry nrtmChannelsRegistry,
                                           final NrtmExceptionHandler exceptionHandler,
                                           final MaintenanceHandler maintenanceHandler,
                                           final NrtmQueryHandlerFactory nrtmQueryHandlerFactory,
                                           final NrtmAclLimitHandler nrtmAclLimitHandler,
                                           final NrtmConnectionPerIpLimitHandler nrtmConnectionPerIpLimitHandler,
                                           final @Value("${proxy.protocol.enabled:false}") boolean proxyProtocolEnabled) {
        this.nrtmChannelsRegistry = nrtmChannelsRegistry;
        this.exceptionHandler = exceptionHandler;
        this.maintenanceHandler = maintenanceHandler;
        this.nrtmQueryHandlerFactory = nrtmQueryHandlerFactory;
        this.nrtmConnectionPerIpLimitHandler = nrtmConnectionPerIpLimitHandler;
        this.nrtmAclLimitHandler = nrtmAclLimitHandler;
        this.proxyProtocolEnabled = proxyProtocolEnabled;

        if (proxyProtocolEnabled) {
            LOGGER.info("Proxy protocol handler enabled");
        }
    }

    @Override
    protected void initChannel(Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();

        pipeline.addLast("U-maintenanceHandler", maintenanceHandler);
        pipeline.addLast("U-acl", nrtmAclLimitHandler);
        pipeline.addLast("connectionPerIpLimit", nrtmConnectionPerIpLimitHandler);

        pipeline.addLast("U-channels", nrtmChannelsRegistry);

        if (proxyProtocolEnabled) {
            pipeline.addLast(ProxyProtocolChannelHandler.NAME, new ProxyProtocolChannelHandler());
        }

        pipeline.addLast("U-delimiter", new DelimiterBasedFrameDecoder(DELIMITER_MAX_FRAME_LENGTH, STRIP_DELIMITER, LINE_DELIMITER));
        pipeline.addLast("U-string-decoder", stringDecoder);
        pipeline.addLast("D-string-encoder", stringEncoder);

        pipeline.addLast(executorGroup, "U-query-handler", nrtmQueryHandlerFactory.getInstance());

        pipeline.addLast("U-exception-handler", exceptionHandler);
    }
}
