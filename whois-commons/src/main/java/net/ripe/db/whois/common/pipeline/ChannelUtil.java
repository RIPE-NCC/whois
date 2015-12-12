package net.ripe.db.whois.common.pipeline;

import com.google.common.base.Charsets;
import org.jboss.netty.channel.Channel;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

public final class ChannelUtil {
    public static final Charset BYTE_ENCODING = Charsets.ISO_8859_1;

    private ChannelUtil() {
        // do not instantiate
    }

    public static InetAddress getRemoteAddress(final Channel channel) {
        final InetAddress inetAddress = ((InetSocketAddress) channel.getRemoteAddress()).getAddress();

        if (inetAddress instanceof Inet6Address) {
            // clean the zone index (% following the ip address)
            try {
                return InetAddress.getByAddress(inetAddress.getAddress());
            } catch (UnknownHostException ignored) {
            }
        }

        return inetAddress;
    }
}
