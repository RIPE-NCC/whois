package net.ripe.db.whois.smtp;

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

@Component
public class SmtpServer implements ApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpServer.class);

    private final SmtpServerChannelsRegistry smtpServerChannelsRegistry;
    private final SmtpServerChannelInitializer smtpServerChannelInitializer;
    private final MaintenanceMode maintenanceMode;
    private final boolean smtpEnabled;
    private final int smtpPort;

    private Channel serverChannel;
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;
    int port;

    @Autowired
    public SmtpServer(
            final SmtpServerChannelsRegistry smtpServerChannelsRegistry,
            final SmtpServerChannelInitializer smtpServerChannelInitializer,
            final MaintenanceMode maintenanceMode,
            @Value("${smtp.enabled:true}") final boolean smtpEnabled,
            @Value("${smtp.port:0}") final int smtpPort) {
        this.smtpServerChannelsRegistry = smtpServerChannelsRegistry;
        this.smtpServerChannelInitializer = smtpServerChannelInitializer;
        this.maintenanceMode = maintenanceMode;
        this.smtpEnabled = smtpEnabled;
        this.smtpPort = smtpPort;
    }

    @Override
    public void start() {
        if (! smtpEnabled) {
            LOGGER.warn("STMP server not enabled");
        } else {
            serverChannel = bootstrapChannel(smtpServerChannelInitializer, smtpPort);
            this.port = ((InetSocketAddress) serverChannel.localAddress()).getPort();
            LOGGER.info("SMTP server listening on port {}", port);
        }
    }

    private Channel bootstrapChannel(final SmtpServerChannelInitializer serverChannelInitializer, final int smtpPort) {
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
            final ChannelFuture channelFuture = bootstrap.bind(new InetSocketAddress(smtpPort)).sync();
            return channelFuture.channel();
        } catch (InterruptedException e) {
            throw new IllegalStateException("NRTM server start up failed", e);
        }
    }

    @Override
    public void stop(final boolean force) {
        if (smtpEnabled) {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            if (force) {
                LOGGER.info("Shutting down");
                if (serverChannel != null) {
                    serverChannel.close();
                    serverChannel = null;
                }

                smtpServerChannelsRegistry.closeChannels();
            } else {
                maintenanceMode.setShutdown();
            }
        }
    }

    public int getPort() {
        return port;
    }
}
