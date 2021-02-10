package net.ripe.db.whois.query;

import com.google.common.util.concurrent.Uninterruptibles;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.ripe.db.whois.common.ApplicationService;
import net.ripe.db.whois.common.MaintenanceMode;
import net.ripe.db.whois.query.pipeline.QueryChannelsRegistry;
import net.ripe.db.whois.query.pipeline.WhoisServerChannelInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

@Component
public final class QueryServer implements ApplicationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryServer.class);

    public static int port;

    @Value("${port.query}") private int queryPort;
    @Value("${loadbalancer.query.timeout:5000}") private int markNodeFailedTimeout;

    private Channel serverChannel;

    private final WhoisServerChannelInitializer whoisServerChannelInitializer;
    private final QueryChannelsRegistry queryChannelsRegistry;
    private final MaintenanceMode maintenanceMode;
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;

    @Autowired
    public QueryServer(final WhoisServerChannelInitializer whoisServerChannelInitializer,
                       final QueryChannelsRegistry queryChannelsRegistry,
                       final MaintenanceMode maintenanceMode) {
        this.whoisServerChannelInitializer = whoisServerChannelInitializer;
        this.queryChannelsRegistry = queryChannelsRegistry;
        this.maintenanceMode = maintenanceMode;
    }

    @Override
    public void start() {


        try {
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            final ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(whoisServerChannelInitializer)
                    // apply TCP options to accepted Channels. Ref. https://netty.io/3.10/guide/
            .option(ChannelOption.SO_BACKLOG, 200)
            .childOption(ChannelOption.TCP_NODELAY, true)
            .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture channelFuture = bootstrap.bind(new InetSocketAddress(queryPort)).sync();
            port = ((InetSocketAddress)channelFuture.channel().localAddress()).getPort();
            LOGGER.info("Query server listening on {}", port);

        } catch (InterruptedException e) {
//            TODO [DA] revisit
            e.printStackTrace();
        }
    }

    @Override
    public void stop(final boolean force) {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        if (serverChannel != null) {
            if (force) {
                Uninterruptibles.sleepUninterruptibly(markNodeFailedTimeout - maintenanceMode.shutdownInitiated(), TimeUnit.MILLISECONDS);
                serverChannel.close();
                serverChannel = null;
                queryChannelsRegistry.closeChannels();
            } else {
                maintenanceMode.setShutdown();
            }
        }
    }
}
