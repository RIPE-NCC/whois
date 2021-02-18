package net.ripe.db.whois.query.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import net.ripe.db.whois.common.ApplicationVersion;
import net.ripe.db.whois.common.pipeline.MaintenanceHandler;
import net.ripe.db.whois.query.handler.QueryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Component
public class WhoisServerChannelInitializer extends ChannelInitializer<Channel> {

    private static final ByteBuf LINE_DELIMITER = Unpooled.wrappedBuffer(new byte[]{'\n'});
    private static final ByteBuf INTERRUPT_DELIMITER = Unpooled.wrappedBuffer(new byte[]{(byte)0xff, (byte)0xf4, (byte)0xff, (byte)0xfd, (byte)0x6});
    private static final int TIMEOUT_SECONDS = 180;

    private final StringDecoder stringDecoder = new StringDecoder(StandardCharsets.UTF_8);
    private final MaintenanceHandler maintenanceHandler;
    private final ConnectionPerIpLimitHandler connectionPerIpLimitHandler;
    private final QueryChannelsRegistry queryChannelsRegistry;
    private final TermsAndConditionsHandler termsAndConditionsHandler;
    private final WhoisEncoder whoisEncoder;
    private final QueryDecoder queryDecoder;
    private final QueryHandler queryHandler;
    private final ApplicationVersion applicationVersion;

    @Autowired
    public WhoisServerChannelInitializer(final MaintenanceHandler maintenanceHandler,
                                         final QueryChannelsRegistry queryChannelsRegistry,
                                         final TermsAndConditionsHandler termsAndConditionsHandler,
                                         final QueryDecoder queryDecoder,
                                         final WhoisEncoder whoisEncoder,
                                         final ConnectionPerIpLimitHandler connectionPerIpLimitHandler,
                                         final QueryHandler queryHandler,
                                         final ApplicationVersion applicationVersion) {
        this.maintenanceHandler = maintenanceHandler;
        this.queryChannelsRegistry = queryChannelsRegistry;
        this.termsAndConditionsHandler = termsAndConditionsHandler;
        this.queryDecoder = queryDecoder;
        this.whoisEncoder = whoisEncoder;
        this.connectionPerIpLimitHandler = connectionPerIpLimitHandler;
        this.queryHandler = queryHandler;
        this.applicationVersion = applicationVersion;
    }

    @Override
    public void initChannel(Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();

        pipeline.addLast("maintenanceHandler", maintenanceHandler);
        pipeline.addLast("connectionPerIpLimit", connectionPerIpLimitHandler);

        pipeline.addLast("query-channels", queryChannelsRegistry);
        pipeline.addLast("read-timeout", new ReadTimeoutHandler(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        pipeline.addLast("write-timeout", new WriteTimeoutHandler(TIMEOUT_SECONDS, TimeUnit.SECONDS));

        pipeline.addLast("terms-conditions", termsAndConditionsHandler);

        pipeline.addLast("delimiter", new DelimiterBasedFrameDecoder(1024, LINE_DELIMITER, INTERRUPT_DELIMITER));

        pipeline.addLast("string-decoder", stringDecoder);
        pipeline.addLast("whois-encoder", whoisEncoder);

        pipeline.addLast("query-decoder", queryDecoder);
        pipeline.addLast("connection-state", new ConnectionStateHandler());

        pipeline.addLast("served-by", new ServedByHandler(applicationVersion.getVersion()));
        pipeline.addLast("whois", new WhoisServerHandler(queryHandler));
        pipeline.addLast("exception", new ExceptionHandler());
    }

}
