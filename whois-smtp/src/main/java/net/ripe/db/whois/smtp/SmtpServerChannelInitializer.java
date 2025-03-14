package net.ripe.db.whois.smtp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import net.ripe.db.whois.common.pipeline.MaintenanceHandler;
import org.springframework.stereotype.Component;

@Component
public class SmtpServerChannelInitializer extends ChannelInitializer<Channel> {

    private static final ByteBuf LINE_DELIMITER = Unpooled.wrappedBuffer(new byte[]{'\n'});
    private static final int DELIMITER_MAX_FRAME_LENGTH = 128;
    private static final boolean STRIP_DELIMITER = true;
    private static final int POOL_SIZE = 32;

    private final EventExecutorGroup executorGroup;
    private final SmtpServerChannelsRegistry channelsRegistry;
    private final SmtpServerExceptionHandler exceptionHandler;
    private final MaintenanceHandler maintenanceHandler;
    private final SmtpCommandHandler commandHandler;
    private final SmtpMessageEncoder messageEncoder;

    protected SmtpServerChannelInitializer(
                                           final SmtpServerChannelsRegistry channelsRegistry,
                                           final SmtpServerExceptionHandler exceptionHandler,
                                           final MaintenanceHandler maintenanceHandler,
                                           final SmtpCommandHandler commandHandler,
                                           final SmtpMessageEncoder messageEncoder) {
        this.executorGroup = new DefaultEventExecutorGroup(POOL_SIZE);
        this.channelsRegistry = channelsRegistry;
        this.exceptionHandler = exceptionHandler;
        this.maintenanceHandler = maintenanceHandler;
        this.commandHandler = commandHandler;
        this.messageEncoder = messageEncoder;
    }

    @Override
    protected void initChannel(Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast("U-maintenanceHandler", maintenanceHandler);
        pipeline.addLast("U-channels", channelsRegistry);
        pipeline.addLast("U-delimiter", new DelimiterBasedFrameDecoder(DELIMITER_MAX_FRAME_LENGTH, STRIP_DELIMITER, LINE_DELIMITER));
        pipeline.addLast("D-message-encoder", messageEncoder);
        pipeline.addLast(executorGroup, "U-command-handler", commandHandler);
        pipeline.addLast("U-exception-handler", exceptionHandler);
    }
}
