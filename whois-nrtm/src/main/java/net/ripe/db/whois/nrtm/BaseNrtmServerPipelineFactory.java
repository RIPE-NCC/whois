package net.ripe.db.whois.nrtm;

import com.google.common.base.Charsets;
import net.ripe.db.whois.common.dao.SerialDao;
import net.ripe.db.whois.common.rpsl.Dummifier;
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
import org.springframework.scheduling.TaskScheduler;


abstract class BaseNrtmServerPipelineFactory implements ChannelPipelineFactory {

    private static final ChannelBuffer LINE_DELIMITER = ChannelBuffers.wrappedBuffer(new byte[]{'\n'});

    private static final long EXECUTOR_TOTAL_MEMORY_LIMIT = 16 * 1024 * 1024;
    private static final long EXECUTOR_PER_CHANNEL_MEMORY_LIMIT = 1024 * 1024;
    private static final int POOL_SIZE = 8;

    private final StringDecoder stringDecoder = new StringDecoder(Charsets.UTF_8);
    private final StringEncoder stringEncoder = new StringEncoder(Charsets.UTF_8);
    private final ExecutionHandler executionHandler = new ExecutionHandler(new OrderedMemoryAwareThreadPoolExecutor(POOL_SIZE, EXECUTOR_PER_CHANNEL_MEMORY_LIMIT, EXECUTOR_TOTAL_MEMORY_LIMIT));
    private final NrtmExceptionHandler exceptionHandler;
    private final AccessControlHandler aclHandler;
    private final SerialDao serialDao;
    private final Dummifier dummifier;
    private final TaskScheduler clientSynchronisationScheduler;
    private final NrtmLog nrtmLog;

    private final String version;
    private final String source;
    private final long updateInterval;

    protected BaseNrtmServerPipelineFactory(final NrtmExceptionHandler exceptionHandler, final AccessControlHandler aclHandler,
                                            final SerialDao serialDao, final NrtmLog nrtmLog, final Dummifier dummifier,
                                            final TaskScheduler clientSynchronisationScheduler, final String version,
                                            final String source, final long updateInterval) {
        this.exceptionHandler = exceptionHandler;
        this.aclHandler = aclHandler;
        this.serialDao = serialDao;
        this.nrtmLog = nrtmLog;
        this.dummifier = dummifier;
        this.clientSynchronisationScheduler = clientSynchronisationScheduler;

        this.version = version;
        this.source = source;
        this.updateInterval = updateInterval;
    }

    @Override
    public ChannelPipeline getPipeline() {
        ChannelPipeline pipeline = Channels.pipeline();

        pipeline.addLast("U-acl", aclHandler);

        pipeline.addLast("U-delimiter", new DelimiterBasedFrameDecoder(128, true, LINE_DELIMITER));
        pipeline.addLast("U-string-decoder", stringDecoder);
        pipeline.addLast("D-string-encoder", stringEncoder);

        pipeline.addLast("UD-execution", executionHandler);

        pipeline.addLast("U-query-handler", new NrtmQueryHandler(serialDao, dummifier, clientSynchronisationScheduler, nrtmLog, version, source, updateInterval));

        pipeline.addLast("U-exception-handler", exceptionHandler);

        return pipeline;
    }
}
