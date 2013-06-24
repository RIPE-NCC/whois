package net.ripe.db.whois.common;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.Executors;

public class ServerHelper {
    public static int getActualPort(final int requestedPort) {
        if (requestedPort == -1) {
            return getAvailablePort();
        } else {
            checkPortAvailable(requestedPort);
        }

        return requestedPort;
    }

    public static int getAvailablePort() {
        try {
            ServerSocket s = new ServerSocket(0);
            s.setReuseAddress(true);
            try {
                return s.getLocalPort();
            } finally {
                s.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void checkPortAvailable(final int port) {
        final ChannelFactory channelFactory = new NioServerSocketChannelFactory(Executors.newFixedThreadPool(1), Executors.newFixedThreadPool(1));

        ServerBootstrap bootstrap = null;
        try {
            bootstrap = new ServerBootstrap(channelFactory);
            bootstrap.setPipelineFactory(Channels.pipelineFactory(Channels.pipeline()));
            bootstrap.bind(new InetSocketAddress(port)).close();
        } finally {
            if (bootstrap != null) {
                bootstrap.releaseExternalResources();
            }
        }
    }

    public static void sleep(long millisecs) {
        try {
            Thread.sleep(millisecs);
        } catch (InterruptedException ignored) {}
    }
}
