package net.ripe.db.whois.smtp;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class SmtpServerChannelInitializer extends ChannelInitializer<Channel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpServerChannelInitializer.class);

    private static final ByteBuf LINE_DELIMITER = Unpooled.wrappedBuffer(new byte[]{'\n'});
    private static final int DELIMITER_MAX_FRAME_LENGTH = 128;
    private static final boolean STRIP_DELIMITER = true;

    private static final int POOL_SIZE = 32;

    private final StringDecoder stringDecoder = new StringDecoder(StandardCharsets.UTF_8);
    private final StringEncoder stringEncoder = new StringEncoder(StandardCharsets.UTF_8);
    private final EventExecutorGroup executorGroup = new DefaultEventExecutorGroup(POOL_SIZE);

    private final SmtpServerChannelsRegistry smtpServerChannelsRegistry;
    private final SmtpServerHandlerFactory smtpServerHandlerFactory;
    private final SmtpServerExceptionHandler exceptionHandler;
    private final MaintenanceHandler maintenanceHandler;

    protected SmtpServerChannelInitializer(
                                           final SmtpServerChannelsRegistry smtpServerChannelsRegistry,
                                           final SmtpServerExceptionHandler exceptionHandler,
                                           final MaintenanceHandler maintenanceHandler,
                                           final SmtpServerHandlerFactory smtpServerHandlerFactory) {
        this.smtpServerChannelsRegistry = smtpServerChannelsRegistry;
        this.exceptionHandler = exceptionHandler;
        this.maintenanceHandler = maintenanceHandler;
        this.smtpServerHandlerFactory = smtpServerHandlerFactory;
    }

    @Override
    protected void initChannel(Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();

        pipeline.addLast("U-maintenanceHandler", maintenanceHandler);
        pipeline.addLast("U-channels", smtpServerChannelsRegistry);
        pipeline.addLast("U-delimiter", new DelimiterBasedFrameDecoder(DELIMITER_MAX_FRAME_LENGTH, STRIP_DELIMITER, LINE_DELIMITER));
        pipeline.addLast("U-string-decoder", stringDecoder);
        pipeline.addLast("D-string-encoder", stringEncoder);

        pipeline.addLast(executorGroup, "U-query-handler", smtpServerHandlerFactory.getInstance());

        pipeline.addLast("U-exception-handler", exceptionHandler);
    }
}
