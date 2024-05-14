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

    private final boolean nrtmEnabled;
    private final int nrtmPort;

    private final NrtmChannelsRegistry nrtmChannelsRegistry;
    private final NrtmServerChannelInitializer nrtmServerChannelInitializer;
    private final MaintenanceMode maintenanceMode;
    private Channel serverChannel;
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;

    private int port;


    @Autowired
    public NrtmServer(final NrtmChannelsRegistry nrtmChannelsRegistry,
                      @Value("${nrtm.enabled:true}") final boolean nrtmEnabled,
                      @Value("${port.nrtm:0}") final int nrtmPort,
                      final NrtmServerChannelInitializer nrtmServerChannelInitializer,
                      final MaintenanceMode maintenanceMode) {
        this.nrtmChannelsRegistry = nrtmChannelsRegistry;
        this.nrtmServerChannelInitializer = nrtmServerChannelInitializer;
        this.maintenanceMode = maintenanceMode;
        this.nrtmPort = nrtmPort;
        this.nrtmEnabled = nrtmEnabled;

    }

    @Override
    public void start() {
        if (!nrtmEnabled) {
            LOGGER.warn("NRTM not enabled");
            return;
        }

        serverChannel = bootstrapChannel(nrtmServerChannelInitializer, nrtmPort);
        port = ((InetSocketAddress) serverChannel.localAddress()).getPort();
        LOGGER.info("NRTM server listening on port {}", port);
    }

    private Channel bootstrapChannel(final NrtmServerChannelInitializer serverChannelInitializer, final int nrtmPort) {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        final ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(serverChannelInitializer)
                .option(ChannelOption.SO_BACKLOG, 200)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        try {
            final ChannelFuture channelFuture = bootstrap.bind(new InetSocketAddress(nrtmPort)).sync();
            return channelFuture.channel();
        } catch (InterruptedException e) {
            throw new IllegalStateException("NRTM server start up failed", e);
        }
    }

    @Override
    public void stop(final boolean force) {
        if (nrtmEnabled) {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
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

    public int getPort() {
        return port;
    }
}
