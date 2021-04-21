package net.ripe.db.whois.common.pipeline;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class ChannelUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelUtil.class);
    public static final Charset BYTE_ENCODING = StandardCharsets.ISO_8859_1;

    private ChannelUtil() {
        // do not instantiate
    }

    public static InetAddress getRemoteAddress(final Channel channel) {
        final InetAddress inetAddress = ((InetSocketAddress) channel.remoteAddress()).getAddress();

        if (inetAddress instanceof Inet6Address) {
            // clean the zone index (% following the ip address)
            try {
                return InetAddress.getByAddress(inetAddress.getAddress());
            } catch (UnknownHostException e) {
                LOGGER.debug("{}: {}", e.getClass().getName(), e.getMessage());
            }
        }

        return inetAddress;
    }
}
