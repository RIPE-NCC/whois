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

import java.nio.charset.StandardCharsets;


abstract class BaseNrtmServerChannelInitializer extends ChannelInitializer<Channel> {

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

    protected BaseNrtmServerChannelInitializer(final NrtmChannelsRegistry nrtmChannelsRegistry,
                                               final NrtmExceptionHandler exceptionHandler,
                                               final MaintenanceHandler maintenanceHandler,
                                               final NrtmQueryHandlerFactory nrtmQueryHandlerFactory,
                                               final NrtmAclLimitHandler nrtmAclLimitHandler,
                                               final NrtmConnectionPerIpLimitHandler nrtmConnectionPerIpLimitHandler) {
        this.nrtmChannelsRegistry = nrtmChannelsRegistry;
        this.exceptionHandler = exceptionHandler;
        this.maintenanceHandler = maintenanceHandler;
        this.nrtmQueryHandlerFactory = nrtmQueryHandlerFactory;
        this.nrtmConnectionPerIpLimitHandler = nrtmConnectionPerIpLimitHandler;
        this.nrtmAclLimitHandler = nrtmAclLimitHandler;
    }

    @Override
    protected void initChannel(Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();

        pipeline.addLast("U-maintenanceHandler", maintenanceHandler);
        pipeline.addLast("U-acl", nrtmAclLimitHandler);
        pipeline.addLast("connectionPerIpLimit", nrtmConnectionPerIpLimitHandler);

        pipeline.addLast("U-channels", nrtmChannelsRegistry);

        pipeline.addLast("U-delimiter", new DelimiterBasedFrameDecoder(DELIMITER_MAX_FRAME_LENGTH, STRIP_DELIMITER, LINE_DELIMITER));
        pipeline.addLast("U-string-decoder", stringDecoder);
        pipeline.addLast("D-string-encoder", stringEncoder);

        pipeline.addLast(executorGroup, "U-query-handler", nrtmQueryHandlerFactory.getInstance());

        pipeline.addLast("U-exception-handler", exceptionHandler);

    }
}
