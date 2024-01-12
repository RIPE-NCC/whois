package net.ripe.db.whois.common.support;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.StandardCharsets;

public final class NettyWhoisClientFactory {

    private NettyWhoisClientFactory() {
    }

    private static Bootstrap createNioSocketChannelBootstrap() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup());
        bootstrap.channel(NioSocketChannel.class);
        return bootstrap;
    }

    public static WhoisClientHandler newLocalClient(int port) {
        Bootstrap bootstrap = createNioSocketChannelBootstrap();
        final WhoisClientHandler clientHandler = new WhoisClientHandler(bootstrap, port);
        initPipeline(bootstrap, clientHandler);
        return clientHandler;
    }

    private static void initPipeline(Bootstrap bootstrap, final WhoisClientHandler clientHandler) {
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {

                ch.pipeline().addLast("encoder", new StringEncoder(StandardCharsets.UTF_8));
                ch.pipeline().addLast("whois-handler", clientHandler);
            }
        });

    }
}
