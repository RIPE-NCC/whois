package net.ripe.db.whois.nrtm;

import com.google.common.base.Charsets;
import net.ripe.db.whois.common.pipeline.MaintenanceHandler;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


abstract class BaseNrtmServerPipelineFactory implements ChannelPipelineFactory {

    private static final ChannelBuffer LINE_DELIMITER = ChannelBuffers.wrappedBuffer(new byte[]{'\n'});

    private static final long MEMORY_SIZE_UNLIMITED = 0;
    private static final long TIMEOUT_SECONDS = 60L;
    private static final int POOL_SIZE = 32;

    private final StringDecoder stringDecoder = new StringDecoder(Charsets.UTF_8);
    private final StringEncoder stringEncoder = new StringEncoder(Charsets.UTF_8);

    protected final ExecutionHandler executionHandler = new ExecutionHandler(
        new OrderedMemoryAwareThreadPoolExecutor(POOL_SIZE, MEMORY_SIZE_UNLIMITED, MEMORY_SIZE_UNLIMITED, TIMEOUT_SECONDS, TimeUnit.SECONDS, new ThreadFactory() {
        private final ThreadGroup threadGroup = new ThreadGroup("nrtm-executor-pool");
        private final AtomicInteger threadNumber = new AtomicInteger();

        @Override
        public Thread newThread(final Runnable r) {
            return new Thread(threadGroup, r, "nrtm-executor-thread-" + threadNumber.incrementAndGet());
        }
    }));

    private final NrtmChannelsRegistry nrtmChannelsRegistry;
    private final NrtmExceptionHandler exceptionHandler;
    private final AccessControlHandler aclHandler;
    private final MaintenanceHandler maintenanceHandler;
    private final NrtmQueryHandlerFactory nrtmQueryHandlerFactory;

    protected BaseNrtmServerPipelineFactory(final NrtmChannelsRegistry nrtmChannelsRegistry,
                                            final NrtmExceptionHandler exceptionHandler,
                                            final AccessControlHandler aclHandler,
                                            final MaintenanceHandler maintenanceHandler,
                                            final NrtmQueryHandlerFactory nrtmQueryHandlerFactory) {
        this.nrtmChannelsRegistry = nrtmChannelsRegistry;
        this.exceptionHandler = exceptionHandler;
        this.aclHandler = aclHandler;
        this.maintenanceHandler = maintenanceHandler;
        this.nrtmQueryHandlerFactory = nrtmQueryHandlerFactory;
    }

    @Override
    public ChannelPipeline getPipeline() {
        ChannelPipeline pipeline = Channels.pipeline();

        pipeline.addLast("U-maintenanceHandler", maintenanceHandler);
        pipeline.addLast("U-channels", nrtmChannelsRegistry);
        pipeline.addLast("U-acl", aclHandler);

        pipeline.addLast("U-delimiter", new DelimiterBasedFrameDecoder(128, true, LINE_DELIMITER));
        pipeline.addLast("U-string-decoder", stringDecoder);
        pipeline.addLast("D-string-encoder", stringEncoder);

        pipeline.addLast("UD-execution", executionHandler);

        pipeline.addLast("U-query-handler", nrtmQueryHandlerFactory.getInstance());

        pipeline.addLast("U-exception-handler", exceptionHandler);

        return pipeline;
    }
}
