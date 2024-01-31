package net.ripe.db.whois.api.httpserver;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
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
 * When HTTP requests are proxied via a loadbalancer, the X-Forwarded-For value will replace the request address.
 * In case of multiple (comma separated) values in X-Forwarded-For, the last value is used.
 */
public class RemoteAddressCustomizer  implements HttpConfiguration.Customizer {

    public static final String QUERY_PARAM_CLIENT_IP = "clientIp";
    private final Set<Interval> trusted;
    private static final Splitter COMMA_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();

    public RemoteAddressCustomizer(final String trustedIpRanges) {
        this.trusted = getIntervals(trustedIpRanges.split(","));
    }

    @Override
    public void customize(final Connector connector, final HttpConfiguration httpConfiguration, final Request request) {
        request.setRemoteAddr(InetSocketAddress.createUnresolved(getRemoteAddress(request), request.getRemotePort()));
    }

    @Nullable
    private String getRemoteAddress(final Request request) {
        final Interval ipResource = IpInterval.asIpInterval(InetAddresses.forString(request.getRemoteAddr()));

        final String clientIp = request.getParameterMap().containsKey(QUERY_PARAM_CLIENT_IP) ? request.getParameter(QUERY_PARAM_CLIENT_IP) : null;
        if(isTrusted(ipResource, trusted) && StringUtils.isNotEmpty(clientIp)) {
            return clientIp;
        }

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

    private Set<Interval> getIntervals(final String[] trusted) {
        return Arrays.stream(trusted).map(IpInterval::parse).collect(Collectors.toSet());
    }

    private boolean isTrusted(final Interval ipResource, final Set<Interval> ipRanges) {
        return ipRanges.stream().anyMatch(ipRange -> ipRange.getClass().equals(ipResource.getClass()) && ipRange.contains(ipResource));
    }
}
