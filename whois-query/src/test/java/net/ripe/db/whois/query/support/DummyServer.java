package net.ripe.db.whois.query.support;

import com.google.common.base.Charsets;
import net.ripe.db.whois.common.ServerHelper;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class DummyServer {
    private final int port = ServerHelper.getAvailablePort();
    private final ChannelFactory factory;
    private final ServerBootstrap bootstrap;

    public DummyServer(final ChannelHandler channelHandler) {
        factory = new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool());

        bootstrap = new ServerBootstrap(factory);

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {
                return Channels.pipeline(
                        new DelimiterBasedFrameDecoder(1024, true, ChannelBuffers.wrappedBuffer(new byte[]{'\n'})),
                        new StringDecoder(Charsets.UTF_8),
                        new StringEncoder(Charsets.UTF_8),
                        channelHandler
                );
            }
        });

        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
    }

    public int getPort() {
        return port;
    }

    public void start() {
        bootstrap.bind(new InetSocketAddress(port));
    }

    public void stop() {
        factory.releaseExternalResources();
    }
}
