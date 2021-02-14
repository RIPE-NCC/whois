package net.ripe.db.whois.nrtm;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import net.ripe.db.whois.common.pipeline.MaintenanceHandler;

import java.nio.charset.StandardCharsets;


abstract class BaseNrtmServerChannelInitializer extends ChannelInitializer<Channel> {

    private static final ByteBuf LINE_DELIMITER = Unpooled.wrappedBuffer(new byte[]{'\n'});
    private final StringDecoder stringDecoder = new StringDecoder(StandardCharsets.UTF_8);
    private final StringEncoder stringEncoder = new StringEncoder(StandardCharsets.UTF_8);
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

        pipeline.addLast("U-delimiter", new DelimiterBasedFrameDecoder(128, true, LINE_DELIMITER));
        pipeline.addLast("U-string-decoder", stringDecoder);
        pipeline.addLast("D-string-encoder", stringEncoder);

        pipeline.addLast("U-query-handler", nrtmQueryHandlerFactory.getInstance());

        pipeline.addLast("U-exception-handler", exceptionHandler);

    }
}
