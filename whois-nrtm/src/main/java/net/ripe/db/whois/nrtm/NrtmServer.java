package net.ripe.db.whois.nrtm;

import net.ripe.db.whois.common.ApplicationService;
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
import java.util.concurrent.Executors;

import static net.ripe.db.whois.common.ServerHelper.getActualPort;

@Component
public class NrtmServer implements ApplicationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmServer.class);

    public static final int NRTM_VERSION = 3;

    @Value("${nrtm.enabled}") private boolean nrtmEnabled;
    @Value("${port.nrtm}") private int nrtmPort;
    @Value("${port.nrtm.legacy}") private int nrtmPortLegacy;

    private final NrtmServerPipelineFactory nrtmServerPipelineFactory;
    private final LegacyNrtmServerPipelineFactory legacyNrtmServerPipelineFactory;
    private Channel serverChannelLegacy;
    private Channel serverChannel;

    public static int port;
    public static int legacyPort;

    @Autowired
    public NrtmServer(final NrtmServerPipelineFactory whoisServerPipelineFactory, final LegacyNrtmServerPipelineFactory legacyNrtmServerPipelineFactory) {
        this.nrtmServerPipelineFactory = whoisServerPipelineFactory;
        this.legacyNrtmServerPipelineFactory = legacyNrtmServerPipelineFactory;
    }

    @Override
    public void start() {
        if (!nrtmEnabled) {
            LOGGER.warn("NRTM not enabled");
            return;
        }

        port = getActualPort(nrtmPort);
        legacyPort = getActualPort(nrtmPortLegacy);

        serverChannelLegacy = bootstrapChannel(legacyNrtmServerPipelineFactory, legacyPort, "OLD DUMMIFER");
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
    public void stop() {
        if (nrtmEnabled) {
            LOGGER.info("Shutting down");

            if (serverChannelLegacy != null) {
                serverChannelLegacy.close();
                serverChannelLegacy = null;
            }

            if (serverChannel != null) {
                serverChannel.close();
                serverChannel = null;
            }
        }
    }
}
