package net.ripe.db.whois.nrtm;

import net.ripe.db.whois.common.ApplicationService;
import net.ripe.db.whois.common.MaintenanceMode;
import net.ripe.db.whois.common.aspects.RetryFor;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipelineFactory;
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
public class NrtmServer implements ApplicationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmServer.class);

    public static final int NRTM_VERSION = 3;

    @Value("${nrtm.enabled}") private boolean nrtmEnabled;
    @Value("${port.nrtm}") private int nrtmPort;
    @Value("${port.nrtm.legacy}") private int nrtmPortLegacy;
    @Value("${loadbalancer.nrtm.timeout:5000") private int markNodeFailedTimeout;

    private final NrtmChannelsRegistry nrtmChannelsRegistry;
    private final NrtmServerPipelineFactory nrtmServerPipelineFactory;
    private final LegacyNrtmServerPipelineFactory legacyNrtmServerPipelineFactory;
    private final MaintenanceMode maintenanceMode;
    private Channel serverChannelLegacy;
    private Channel serverChannel;

    public static int port;
    public static int legacyPort;

    @Autowired
    public NrtmServer(final NrtmChannelsRegistry nrtmChannelsRegistry,
                      final NrtmServerPipelineFactory whoisServerPipelineFactory,
                      final LegacyNrtmServerPipelineFactory legacyNrtmServerPipelineFactory,
                      final MaintenanceMode maintenanceMode) {
        this.nrtmChannelsRegistry = nrtmChannelsRegistry;
        this.nrtmServerPipelineFactory = whoisServerPipelineFactory;
        this.legacyNrtmServerPipelineFactory = legacyNrtmServerPipelineFactory;
        this.maintenanceMode = maintenanceMode;
    }

    @Override
    public void start() {
        if (!nrtmEnabled) {
            LOGGER.warn("NRTM not enabled");
            return;
        }

        resumeLegacy();
        resume();
    }

    @RetryFor(ChannelException.class)
    public void resumeLegacy() {
        legacyPort = getActualPort(nrtmPortLegacy);
        serverChannelLegacy = bootstrapChannel(legacyNrtmServerPipelineFactory, legacyPort, "OLD DUMMIFER");
    }

    @RetryFor(ChannelException.class)
    public void resume() {
        port = getActualPort(nrtmPort);
        serverChannel = bootstrapChannel(nrtmServerPipelineFactory, port, "NEW DUMMIFER");
    }

    private Channel bootstrapChannel(final ChannelPipelineFactory serverPipelineFactory, final int port, final String instanceName) {
        final ChannelFactory channelFactory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());

        ServerBootstrap bootstrap = new ServerBootstrap(channelFactory);

        bootstrap.setPipelineFactory(serverPipelineFactory);

        bootstrap.setOption("backlog", 200);
        bootstrap.setOption("child.keepAlive", true);

        LOGGER.info("NRTM server listening on port {} ({})", port, instanceName);
        return bootstrap.bind(new InetSocketAddress(port));
    }

    @Override
    public void stop(final boolean force) {
        if (nrtmEnabled) {
            if (force) {
                LOGGER.info("Shutting down");

                if (serverChannelLegacy != null) {
                    serverChannelLegacy.close();
                    serverChannelLegacy = null;
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
}
