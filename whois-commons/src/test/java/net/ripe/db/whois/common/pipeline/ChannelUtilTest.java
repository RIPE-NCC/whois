package net.ripe.db.whois.common.pipeline;

import com.google.common.net.InetAddresses;
import org.jboss.netty.channel.*;
import org.junit.Test;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

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
        public SocketAddress getRemoteAddress() {
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
        public ChannelConfig getConfig() {
            return null;
        }

        @Override
        public boolean isBound() {
            return false;
        }

        @Override
        public boolean isConnected() {
            return false;
        }

        @Override
        public SocketAddress getLocalAddress() {
            return null;
        }

        @Override
        public int compareTo(Channel o) {
            return 0;
        }

        @Override
        public Integer getId() {
            return null;
        }

        @Override
        public ChannelFactory getFactory() {
            return null;
        }

        @Override
        public Channel getParent() {
            return null;
        }

        @Override
        public ChannelPipeline getPipeline() {
            return null;
        }

        @Override
        public boolean isOpen() {
            return false;
        }

        @Override
        public ChannelFuture write(Object message) {
            return null;
        }

        @Override
        public ChannelFuture write(Object message, SocketAddress remoteAddress) {
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
        public ChannelFuture disconnect() {
            return null;
        }

        @Override
        public ChannelFuture unbind() {
            return null;
        }

        @Override
        public ChannelFuture close() {
            return null;
        }

        @Override
        public ChannelFuture getCloseFuture() {
            return null;
        }

        @Override
        public int getInterestOps() {
            return 0;
        }

        @Override
        public boolean isReadable() {
            return false;
        }

        @Override
        public boolean isWritable() {
            return false;
        }

        @Override
        public ChannelFuture setInterestOps(int interestOps) {
            return null;
        }

        @Override
        public ChannelFuture setReadable(boolean readable) {
            return null;
        }
    }
}
