package net.ripe.db.whois.common.support;

import com.google.common.base.Charsets;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.string.StringEncoder;

import java.util.concurrent.Executors;

public final class NettyWhoisClientFactory {

    private NettyWhoisClientFactory() {
    }

    private static NioClientSocketChannelFactory createChannelFactory() {
        return new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool(), 1, 1);
    }

    public static WhoisClientHandler newLocalClient(int port) {
        ClientBootstrap bootstrap = new ClientBootstrap(createChannelFactory());
        final WhoisClientHandler clientHandler = new WhoisClientHandler(bootstrap, port);
        initPipeline(bootstrap, clientHandler);
        return clientHandler;
    }

    public static WhoisClientHandler newExternalClient(String hostName, int port) {
        ClientBootstrap bootstrap = new ClientBootstrap(createChannelFactory());
        final WhoisClientHandler clientHandler = new WhoisClientHandler(bootstrap, hostName, port);
        initPipeline(bootstrap, clientHandler);
        return clientHandler;
    }

    private static void initPipeline(ClientBootstrap bootstrap, final WhoisClientHandler clientHandler) {
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();

                pipeline.addLast("encoder", new StringEncoder(Charsets.UTF_8));
                pipeline.addLast("whois-handler", clientHandler);

                return pipeline;
            }
        });
    }
}
