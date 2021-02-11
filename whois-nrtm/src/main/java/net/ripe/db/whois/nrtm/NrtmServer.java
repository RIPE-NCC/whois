package net.ripe.db.whois.nrtm;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.ripe.db.whois.common.ApplicationService;
import net.ripe.db.whois.common.MaintenanceMode;
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

    @Value("${nrtm.enabled}") private boolean nrtmEnabled;
    @Value("${port.nrtm}") private int nrtmPort;

    private final NrtmChannelsRegistry nrtmChannelsRegistry;
    private final NrtmServerChannelInitializer nrtmServerChannelInitializer;
    private final MaintenanceMode maintenanceMode;
    private Channel serverChannel;
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;

    private static int port;


    @Autowired
    public NrtmServer(final NrtmChannelsRegistry nrtmChannelsRegistry,
                      final NrtmServerChannelInitializer nrtmServerChannelInitializer,
                      final MaintenanceMode maintenanceMode) {
        this.nrtmChannelsRegistry = nrtmChannelsRegistry;
        this.nrtmServerChannelInitializer = nrtmServerChannelInitializer;
        this.maintenanceMode = maintenanceMode;
    }

    @Override
    public void start() {
        if (!nrtmEnabled) {
            LOGGER.warn("NRTM not enabled");
            return;
        }

        bootstrapChannel(nrtmServerChannelInitializer, nrtmPort, "NRTM DUMMIFIER");
    }

    private void bootstrapChannel(final NrtmServerChannelInitializer serverChannelInitializer, final int nrtmPort, final String instanceName) {
        try {

            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();

            final ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(serverChannelInitializer)
                    .option(ChannelOption.SO_BACKLOG, 200)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture channelFuture = bootstrap.bind(new InetSocketAddress(nrtmPort)).sync();
            port = ((InetSocketAddress)channelFuture.channel().localAddress()).getPort();
            LOGGER.info("NRTM server listening on port {} ({})", port, instanceName);

        } catch (InterruptedException e) {
            LOGGER.info("NRTM server start up failed due to {}", e.getMessage());
        }
    }

    @Override
    public void stop(final boolean force) {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        NrtmServer.port = 0;
        if (nrtmEnabled) {
            if (force) {
                LOGGER.info("Shutting down");
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
