package net.ripe.db.whois.common.pipeline;

import com.google.common.net.InetAddresses;
import io.netty.buffer.ByteBufAllocator;

import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelProgressivePromise;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.junit.jupiter.api.Test;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ChannelUtilTest {

    @Test
    public void shouldGetRemoteAddressFromChannel() {
        InetAddress remoteAddress = ChannelUtil.getRemoteAddress(new StubbedChannel("192.168.0.1"));

        assertThat(remoteAddress.getHostAddress(), is("192.168.0.1"));
    }

    @Test
    public void shouldGetRemoteAddressFromIpv6AddressChannel() {
        InetAddress remoteAddress = ChannelUtil.getRemoteAddress(new StubbedChannel("2001:67c:2e8:13:1146:e6f4:bfd7:c324"));

        assertThat(remoteAddress.getHostAddress(), is("2001:67c:2e8:13:1146:e6f4:bfd7:c324"));
    }

    @Test
    public void shouldGetRemoteAddressFromIpv6WithoutInterface() {
        InetAddress remoteAddress = ChannelUtil.getRemoteAddress(new StubbedChannel("2001:67c:2e8:13:1146:e6f4:bfd7:c324", true));

        assertThat(remoteAddress.getHostAddress(), is("2001:67c:2e8:13:1146:e6f4:bfd7:c324"));
    }

    private static class StubbedChannel implements Channel {
        private final String ipAddress;
        private final boolean withInterface;
        private final int port = 43;

        public StubbedChannel(String ipAddress) {
            this(ipAddress, false);
        }

        public StubbedChannel(String ipAddress, boolean withInterface) {
            this.ipAddress = ipAddress;
            this.withInterface = withInterface;
        }

        @Override
        public ChannelId id() {
            return null;
        }

        @Override
        public EventLoop eventLoop() {
            return null;
        }

        @Override
        public Channel parent() {
            return null;
        }

        @Override
        public ChannelConfig config() {
            return null;
        }

        @Override
        public boolean isOpen() {
            return false;
        }

        @Override
        public boolean isRegistered() {
            return false;
        }

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public ChannelMetadata metadata() {
            return null;
        }

        @Override
        public SocketAddress localAddress() {
            return null;
        }

        @Override
        public SocketAddress remoteAddress() {
            InetAddress inetAddress = InetAddresses.forString(ipAddress);

            if (withInterface) {
                try {
                    inetAddress = Inet6Address.getByAddress("[localhost]", inetAddress.getAddress(), 1);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            return new InetSocketAddress(inetAddress, port);
        }

        @Override
        public ChannelFuture closeFuture() {
            return null;
        }

        @Override
        public ChannelFuture write(Object message) {
            return null;
        }

        @Override
        public ChannelFuture write(Object o, ChannelPromise channelPromise) {
            return null;
        }

        @Override
        public ChannelFuture bind(SocketAddress localAddress) {
            return null;
        }

        @Override
        public ChannelFuture connect(SocketAddress remoteAddress) {
            return null;
        }

        @Override
        public ChannelFuture connect(SocketAddress socketAddress, SocketAddress socketAddress1) {
            return null;
        }

        @Override
        public ChannelFuture disconnect() {
            return null;
        }

        @Override
        public ChannelFuture close() {
            return null;
        }

        @Override
        public ChannelFuture deregister() {
            return null;
        }

        @Override
        public ChannelFuture bind(SocketAddress socketAddress, ChannelPromise channelPromise) {
            return null;
        }

        @Override
        public ChannelFuture connect(SocketAddress socketAddress, ChannelPromise channelPromise) {
            return null;
        }

        @Override
        public ChannelFuture connect(SocketAddress socketAddress, SocketAddress socketAddress1, ChannelPromise channelPromise) {
            return null;
        }

        @Override
        public ChannelFuture disconnect(ChannelPromise channelPromise) {
            return null;
        }

        @Override
        public ChannelFuture close(ChannelPromise channelPromise) {
            return null;
        }

        @Override
        public ChannelFuture deregister(ChannelPromise channelPromise) {
            return null;
        }

        @Override
        public boolean isWritable() {
            return false;
        }

        @Override
        public long bytesBeforeUnwritable() {
            return 0;
        }

        @Override
        public long bytesBeforeWritable() {
            return 0;
        }

        @Override
        public Unsafe unsafe() {
            return null;
        }

        @Override
        public ChannelPipeline pipeline() {
            return null;
        }

        @Override
        public ByteBufAllocator alloc() {
            return null;
        }

        @Override
        public Channel read() {
            return null;
        }

        @Override
        public Channel flush() {
            return null;
        }

        @Override
        public ChannelFuture writeAndFlush(Object o, ChannelPromise channelPromise) {
            return null;
        }

        @Override
        public ChannelFuture writeAndFlush(Object o) {
            return null;
        }

        @Override
        public ChannelPromise newPromise() {
            return null;
        }

        @Override
        public ChannelProgressivePromise newProgressivePromise() {
            return null;
        }

        @Override
        public ChannelFuture newSucceededFuture() {
            return null;
        }

        @Override
        public ChannelFuture newFailedFuture(Throwable throwable) {
            return null;
        }

        @Override
        public ChannelPromise voidPromise() {
            return null;
        }

        @Override
        public <T> Attribute<T> attr(AttributeKey<T> attributeKey) {
            return null;
        }

        @Override
        public <T> boolean hasAttr(AttributeKey<T> attributeKey) {
            return false;
        }

        @Override
        public int compareTo(Channel o) {
            return 0;
        }
    }
}
