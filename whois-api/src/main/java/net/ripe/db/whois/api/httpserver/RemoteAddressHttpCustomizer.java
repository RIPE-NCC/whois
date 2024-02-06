package net.ripe.db.whois.api.httpserver;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.net.HttpHeaders;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.common.ip.Interval;
import net.ripe.db.whois.common.ip.IpInterval;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.Request;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * When HTTP requests are proxied via a loadbalancer and  clientIp is set use it as remote Address
 * When HTTP requests are proxied via a loadbalancer and clientIp is not set, the X-Forwarded-For value will replace the request address.
 * In case of multiple (comma separated) values in X-Forwarded-For, the last value is used.
 */
public class RemoteAddressHttpCustomizer extends RemoteAddressCustomizer {

    private static final Splitter COMMA_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();

    public RemoteAddressHttpCustomizer(final String trustedIpRanges) {
        super(trustedIpRanges);
    }

    @Override
    String getRemoteAddrForScheme(final Request request) {
        final Enumeration<String> headers = request.getHeaders(HttpHeaders.X_FORWARDED_FOR);

        if (headers == null || !headers.hasMoreElements()) {
            return request.getRemoteAddr();
        }

        final String header = headers.nextElement();
        if (Strings.isNullOrEmpty(header)) {
            return request.getRemoteAddr();
        }

        return Iterables.getLast(COMMA_SPLITTER.split(header));
    }
}
