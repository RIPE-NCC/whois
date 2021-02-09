package net.ripe.db.whois.common.support;

import com.google.common.net.InetAddresses;
import com.jayway.awaitility.Awaitility;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;

public class WhoisClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisClientHandler.class);

    private final Bootstrap bootstrap;
    private final InetSocketAddress host;
    private Channel channel;
    private final ByteBuf response = Unpooled.buffer(8192);
    private boolean success;

    public WhoisClientHandler(Bootstrap bootstrap, String hostName, int port) {
        this.bootstrap = bootstrap;
        this.host = new InetSocketAddress(hostName, port);
    }

    // avoid DNS lookups
    public WhoisClientHandler(Bootstrap bootstrap, int port) {
        this.bootstrap = bootstrap;
        this.host = new InetSocketAddress(InetAddresses.forString("127.0.0.1"), port);
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        channel = ctx.channel();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        synchronized (response) {
            response.writeBytes(((ByteBuf) msg));
        }
        success = true;
    }

    public void clearBuffer() {
        synchronized (response) {
            response.clear();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("Unexpected exception from downstream", cause);
        success = false;
        ctx.channel().close();
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
        ChannelFuture future = channel.writeAndFlush(query + "\n");
        success = future.await(10, TimeUnit.SECONDS);
        return future;
    }

    public boolean getSuccess() {
        return success;
    }

    public String getResponse() {
        synchronized (response) {
            return response.toString(StandardCharsets.UTF_8);
        }
    }

    public void waitForClose() throws InterruptedException {
        success = channel.closeFuture().await(3, TimeUnit.SECONDS);
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
