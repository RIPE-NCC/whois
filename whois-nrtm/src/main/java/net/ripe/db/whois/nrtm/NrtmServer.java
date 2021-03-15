package net.ripe.db.whois.nrtm;

import net.ripe.db.whois.common.ApplicationService;
import net.ripe.db.whois.common.MaintenanceMode;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

// TODO: [ES] refactor use of two variables (one static) for port number
@Component
public class NrtmServer implements ApplicationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmServer.class);

    public static final int NRTM_VERSION = 3;

    @Value("${nrtm.enabled:true}") private boolean nrtmEnabled;
    @Value("${port.nrtm:0}") private int nrtmPort;

    private final NrtmChannelsRegistry nrtmChannelsRegistry;
    private final NrtmServerPipelineFactory nrtmServerPipelineFactory;
    private final MaintenanceMode maintenanceMode;
    private Channel serverChannel;
    private ChannelFactory channelFactory;

    private static int port;


    @Autowired
    public NrtmServer(final NrtmChannelsRegistry nrtmChannelsRegistry,
                      final NrtmServerPipelineFactory nrtmServerPipelineFactory,
                      final MaintenanceMode maintenanceMode) {
        this.nrtmChannelsRegistry = nrtmChannelsRegistry;
        this.nrtmServerPipelineFactory = nrtmServerPipelineFactory;
        this.maintenanceMode = maintenanceMode;
    }

    @Override
    public void start() {
        if (!nrtmEnabled) {
            LOGGER.warn("NRTM not enabled");
            return;
        }

        serverChannel = bootstrapChannel(nrtmServerPipelineFactory, nrtmPort, "NRTM DUMMIFIER");
        port = ((InetSocketAddress) serverChannel.getLocalAddress()).getPort();
    }

    private Channel bootstrapChannel(final ChannelPipelineFactory serverPipelineFactory, final int port, final String instanceName) {
        channelFactory = new NioServerSocketChannelFactory();
        final ServerBootstrap bootstrap = new ServerBootstrap(channelFactory);
        bootstrap.setPipelineFactory(serverPipelineFactory);
        bootstrap.setOption("backlog", 200);
        // apply TCP options to accepted Channels. Ref. https://netty.io/3.10/guide/
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);

        final Channel channel = bootstrap.bind(new InetSocketAddress(port));
        final int actualPort = ((InetSocketAddress) channel.getLocalAddress()).getPort();
        LOGGER.info("NRTM server listening on port {} ({})", actualPort, instanceName);
        return channel;
    }

    @Override
    public void stop(final boolean force) {
        if (nrtmEnabled) {
            if (force) {
                LOGGER.info("Shutting down");
                if (channelFactory != null) {
                    channelFactory.shutdown();
                }
                if (serverChannel != null) {
                    serverChannel.close();
                    serverChannel = null;
                }

                nrtmChannelsRegistry.closeChannels();
            } else {
                maintenanceMode.setShutdown();
            }
        }
    }

    public static int getPort() {
        return port;
    }

}
