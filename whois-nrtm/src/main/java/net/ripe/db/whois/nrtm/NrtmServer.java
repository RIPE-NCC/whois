package net.ripe.db.whois.nrtm;

import net.ripe.db.whois.common.ApplicationService;
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
public class NrtmServer implements ApplicationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmServer.class);

    public static final int NRTM_VERSION = 3;

    @Value("${nrtm.enabled}") private boolean nrtmEnabled;
    @Value("${port.nrtm}") private int nrtmPort;

    private final NrtmServerPipelineFactory nrtmServerPipelineFactory;
    private Channel serverChannel;

    public static int port;

    @Autowired
    public NrtmServer(final NrtmServerPipelineFactory whoisServerPipelineFactory) {
        this.nrtmServerPipelineFactory = whoisServerPipelineFactory;
    }

    @Override
    public void start() {
        if (!nrtmEnabled) {
            LOGGER.warn("NRTM not enabled");
            return;
        }

        port = getActualPort(nrtmPort);

        final ChannelFactory channelFactory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());

        ServerBootstrap bootstrap = new ServerBootstrap(channelFactory);

        bootstrap.setPipelineFactory(nrtmServerPipelineFactory);

        bootstrap.setOption("backlog", 200);
        bootstrap.setOption("child.keepAlive", true);

        serverChannel = bootstrap.bind(new InetSocketAddress(port));
        LOGGER.info("NRTM server listening on port {}", port);
    }

    @Override
    public void stop() {
        if (nrtmEnabled) {
            LOGGER.info("Shutting down");

            if (serverChannel != null) {
                serverChannel.close();
                serverChannel = null;
            }
        }
    }
}
