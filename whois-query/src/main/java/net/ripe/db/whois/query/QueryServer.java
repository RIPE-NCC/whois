package net.ripe.db.whois.query;

import net.ripe.db.whois.common.ApplicationService;
import net.ripe.db.whois.common.MaintenanceMode;
import net.ripe.db.whois.common.ServerHelper;
import net.ripe.db.whois.query.pipeline.QueryChannelsRegistry;
import net.ripe.db.whois.query.pipeline.WhoisServerPipelineFactory;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import static net.ripe.db.whois.common.ServerHelper.getActualPort;

@Component
public final class QueryServer implements ApplicationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryServer.class);

    public static int port;

    @Value("${port.query}") private int queryPort;
    @Value("${application.version}") private String version;
    @Value("${loadbalancer.query.timeout:5000}") private int markNodeFailedTimeout;

    private Channel serverChannel;

    private final WhoisServerPipelineFactory whoisServerPipelineFactory;
    private final QueryChannelsRegistry queryChannelsRegistry;
    private final ChannelFactory channelFactory;
    private final MaintenanceMode maintenanceMode;

    @Autowired
    public QueryServer(final WhoisServerPipelineFactory whoisServerPipelineFactory,
                       final QueryChannelsRegistry queryChannelsRegistry,
                       final MaintenanceMode maintenanceMode) {
        this.whoisServerPipelineFactory = whoisServerPipelineFactory;
        this.queryChannelsRegistry = queryChannelsRegistry;
        this.maintenanceMode = maintenanceMode;
        this.channelFactory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
    }

    @Override
    public void start() {
        port = getActualPort(queryPort);

        final ServerBootstrap bootstrap = new ServerBootstrap(channelFactory);
        bootstrap.setPipelineFactory(whoisServerPipelineFactory);
        bootstrap.setOption("backlog", 200);
        bootstrap.setOption("child.keepAlive", true);

        serverChannel = bootstrap.bind(new InetSocketAddress(port));
        LOGGER.info("Query server listening on {}", port);
    }

    @Override
    public void stop(final boolean force) {
        if (serverChannel != null) {
            if (force) {
                ServerHelper.sleep(markNodeFailedTimeout - maintenanceMode.shutdownInitiated());
                serverChannel.close();
                serverChannel = null;

                queryChannelsRegistry.closeChannels();
            } else {
                maintenanceMode.setShutdown();
            }
        }
    }
}
