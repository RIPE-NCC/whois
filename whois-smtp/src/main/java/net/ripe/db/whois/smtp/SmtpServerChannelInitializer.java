package net.ripe.db.whois.smtp;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import net.ripe.db.whois.common.pipeline.MaintenanceHandler;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class SmtpServerChannelInitializer extends ChannelInitializer<Channel> {

    private static final int DELIMITER_MAX_FRAME_LENGTH = 1024;
    private static final boolean STRIP_DELIMITER = true;
    private static final int POOL_SIZE = 32;
    private static final int TIMEOUT = 60;

    private final EventExecutorGroup executorGroup;
    private final SmtpServerChannelsRegistry channelsRegistry;
    private final SmtpServerExceptionHandler exceptionHandler;
    private final MaintenanceHandler maintenanceHandler;
    private final SmtpCommandHandler commandHandler;
    private final SmtpResponseEncoder responseEncoder;

    protected SmtpServerChannelInitializer(
                                           final SmtpServerChannelsRegistry channelsRegistry,
                                           final SmtpServerExceptionHandler exceptionHandler,
                                           final MaintenanceHandler maintenanceHandler,
                                           final SmtpCommandHandler commandHandler,
                                           final SmtpResponseEncoder responseEncoder) {
        this.executorGroup = new DefaultEventExecutorGroup(POOL_SIZE);
        this.channelsRegistry = channelsRegistry;
        this.exceptionHandler = exceptionHandler;
        this.maintenanceHandler = maintenanceHandler;
        this.commandHandler = commandHandler;
        this.responseEncoder = responseEncoder;
    }

    @Override
    protected void initChannel(Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast("maintenanceHandler", maintenanceHandler);
        pipeline.addLast("channels", channelsRegistry);
        pipeline.addLast("read-timeout", new ReadTimeoutHandler(TIMEOUT, TimeUnit.SECONDS) {
            @Override
            protected void readTimedOut(ChannelHandlerContext ctx) {
                // keep connection open on read timeout so we can write a message to the client
                ctx.fireExceptionCaught(ReadTimeoutException.INSTANCE);
            }
        });
        pipeline.addLast("write-timeout", new WriteTimeoutHandler(TIMEOUT, TimeUnit.SECONDS));
        pipeline.addLast("delimiter", new DelimiterBasedFrameDecoder(DELIMITER_MAX_FRAME_LENGTH, STRIP_DELIMITER, Delimiters.lineDelimiter()));
        pipeline.addLast("response-encoder", responseEncoder);
        pipeline.addLast(executorGroup, "command-handler", commandHandler);
        pipeline.addLast("exception-handler", exceptionHandler);
    }
}
