package net.ripe.db.whois.common.support;

import com.google.common.base.Charsets;
import com.google.common.net.InetAddresses;
import com.jayway.awaitility.Awaitility;
import org.hamcrest.Matcher;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;

public class WhoisClientHandler extends SimpleChannelUpstreamHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisClientHandler.class);

    private final ClientBootstrap bootstrap;
    private final InetSocketAddress host;
    private Channel channel;

    private final ChannelBuffer response = ChannelBuffers.dynamicBuffer(8192);
    private boolean success;

    public WhoisClientHandler(ClientBootstrap bootstrap, String hostName, int port) {
        this.bootstrap = bootstrap;
        this.host = new InetSocketAddress(hostName, port);
    }

    // avoid DNS lookups
    public WhoisClientHandler(ClientBootstrap bootstrap, int port) {
        this.bootstrap = bootstrap;
        this.host = new InetSocketAddress(InetAddresses.forString("127.0.0.1"), port);
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        channel = e.getChannel();
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        ChannelBuffer cb = (ChannelBuffer) e.getMessage();
        synchronized (response) {
            response.writeBytes(cb);
        }
        success = true;
    }

    public void clearBuffer() {
        synchronized (response) {
            response.clear();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        LOGGER.error("Unexpected exception from downstream", e.getCause());
        success = false;
        e.getChannel().close();
    }

    public ChannelFuture connectAndWait() throws InterruptedException {
        ChannelFuture future = bootstrap.connect(host);
        success = future.await(3, TimeUnit.SECONDS);
        return future;
    }

    public ChannelFuture disconnect() {
        return channel.close();
    }

    public ChannelFuture sendLine(String query) throws InterruptedException {
        Assert.assertTrue(success);
        ChannelFuture future = channel.write(query + "\n");
        success = future.await(3, TimeUnit.SECONDS);
        return future;
    }

    public boolean getSuccess() {
        return success;
    }

    public String getResponse() {
        synchronized (response) {
            return response.toString(Charsets.UTF_8);
        }
    }

    public void waitForClose() throws InterruptedException {
        success = channel.getCloseFuture().await(3, TimeUnit.SECONDS);
    }

    public void waitForResponseContains(final String assertText) throws Exception {
        waitForResponse(containsString(assertText));
    }

    public void waitForResponseEndsWith(final String assertText) throws Exception {
        waitForResponse(endsWith(assertText));
    }

    private void waitForResponse(Matcher<String> anwserMatcher) throws Exception {
        Awaitility.waitAtMost(3, TimeUnit.SECONDS).until(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return getResponse();
            }
        }, anwserMatcher);
    }
}
