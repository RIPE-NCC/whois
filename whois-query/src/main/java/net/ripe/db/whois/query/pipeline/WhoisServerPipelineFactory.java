package net.ripe.db.whois.query.pipeline;

import com.google.common.base.Charsets;
import net.ripe.db.whois.query.handler.QueryHandler;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.handler.timeout.WriteTimeoutHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class WhoisServerPipelineFactory implements ChannelPipelineFactory {

    private static final ChannelBuffer LINE_DELIMITER = ChannelBuffers.wrappedBuffer(new byte[]{'\n'});
    private static final Timer TIMER = new HashedWheelTimer();
    private static final int TIMEOUT_SECONDS = 180;
    private static final int POOL_SIZE = 64;
    private static final int MEMORY_SIZE_UNLIMITED = 0;

    @Value("${application.version}") private String version;

    private final ReadTimeoutHandler readTimeoutHandler = new ReadTimeoutHandler(TIMER, TIMEOUT_SECONDS, TimeUnit.SECONDS);
    private final WriteTimeoutHandler writeTimeoutHandler = new WriteTimeoutHandler(TIMER, TIMEOUT_SECONDS, TimeUnit.SECONDS);
    private final StringDecoder stringDecoder = new StringDecoder(Charsets.UTF_8);

    private final ExecutionHandler executionHandler = new ExecutionHandler(new OrderedMemoryAwareThreadPoolExecutor(
            POOL_SIZE, MEMORY_SIZE_UNLIMITED, MEMORY_SIZE_UNLIMITED, 30, TimeUnit.SECONDS, new ThreadFactory() {
        private final ThreadGroup threadGroup = new ThreadGroup("executor-pool");
        private final AtomicInteger threadNumber = new AtomicInteger();

        @Override
        public Thread newThread(final Runnable r) {
            return new Thread(threadGroup, r, "executor-thread-" + threadNumber.incrementAndGet());
        }
    }));

    private final ConnectionPerIpLimitHandler connectionPerIpLimitHandler;
    private final QueryChannelsRegistry queryChannelsRegistry;
    private final TermsAndConditionsHandler termsAndConditionsHandler;
    private final WhoisEncoder whoisEncoder;
    private final QueryDecoder queryDecoder;
    private final QueryHandler queryHandler;

    @Autowired
    public WhoisServerPipelineFactory(final QueryChannelsRegistry queryChannelsRegistry,
                                      final TermsAndConditionsHandler termsAndConditionsHandler,
                                      final QueryDecoder queryDecoder,
                                      final WhoisEncoder whoisEncoder,
                                      final ConnectionPerIpLimitHandler connectionPerIpLimitHandler,
                                      final QueryHandler queryHandler) {
        this.queryChannelsRegistry = queryChannelsRegistry;
        this.termsAndConditionsHandler = termsAndConditionsHandler;
        this.queryDecoder = queryDecoder;
        this.whoisEncoder = whoisEncoder;
        this.connectionPerIpLimitHandler = connectionPerIpLimitHandler;
        this.queryHandler = queryHandler;
    }

    @PreDestroy
    private void destroyExecutionHandler() {
        executionHandler.releaseExternalResources();
    }

    @Override
    public ChannelPipeline getPipeline() {
        final ChannelPipeline pipeline = Channels.pipeline();

        pipeline.addLast("connectionPerIpLimit", connectionPerIpLimitHandler);

        pipeline.addLast("query-channels", queryChannelsRegistry);
        pipeline.addLast("read-timeout", readTimeoutHandler);
        pipeline.addLast("write-timeout", writeTimeoutHandler);

        pipeline.addLast("terms-conditions", termsAndConditionsHandler);

        pipeline.addLast("delimiter", new DelimiterBasedFrameDecoder(1024, true, LINE_DELIMITER));
        pipeline.addLast("string-decoder", stringDecoder);
        pipeline.addLast("whois-encoder", whoisEncoder);

        pipeline.addLast("execution", executionHandler);

        pipeline.addLast("exception", new ExceptionHandler());
        pipeline.addLast("query-decoder", queryDecoder);
        pipeline.addLast("connection-state", new ConnectionStateHandler());

        pipeline.addLast("served-by", new ServedByHandler(version));
        pipeline.addLast("whois", new WhoisServerHandler(queryHandler));

        return pipeline;
    }
}
