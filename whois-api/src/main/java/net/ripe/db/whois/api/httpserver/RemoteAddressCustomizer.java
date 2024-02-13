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

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * When HTTP requests via trusted source use clientIp query paramater as remote Address if set
 */
public class RemoteAddressCustomizer implements HttpConfiguration.Customizer {

    private static final Splitter COMMA_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();
    public static final String QUERY_PARAM_CLIENT_IP = "clientIp";
    private final Set<Interval> trusted;

    private final boolean usingForwardedForHeader;

    public RemoteAddressCustomizer(final String trustedIpRanges, final boolean xforwardedFor) {
        this.trusted = getIntervals(trustedIpRanges.split(","));
        this.usingForwardedForHeader = xforwardedFor;
    }

    @Override
    public void customize(final Connector connector, final HttpConfiguration httpConfiguration, final Request request) {

        final String remoteAddress = getRemoteAddrForTrustedSource(request, getRemoteAddrForScheme(request));
        request.setRemoteAddr(InetSocketAddress.createUnresolved(remoteAddress, request.getRemotePort()));
    }

    private String getRemoteAddrForTrustedSource(final Request request, final String address) {
        final String resourceAddr = (address.startsWith("[") && address.endsWith("]")) ? address.substring(1, address.length() - 1) : address;
        final Interval ipResource = IpInterval.asIpInterval(InetAddresses.forString(resourceAddr));

        if (request.getQueryParameters() != null){
            final String clientIp = request.getQueryParameters().containsKey(QUERY_PARAM_CLIENT_IP) ? request.getParameter(QUERY_PARAM_CLIENT_IP) : null;
            if (StringUtils.isNotEmpty(clientIp) && isTrusted(ipResource, trusted)) {
                return clientIp;
            }
        }

        return resourceAddr;
    }

    private String getRemoteAddrForScheme(final Request request){
        if (usingForwardedForHeader){
            final Enumeration<String> headers = request.getHeaders(HttpHeaders.X_FORWARDED_FOR);

            if (headers == null || !headers.hasMoreElements()) {
                return request.getRemoteAddr();
            }

            final String header = headers.nextElement();
            if (Strings.isNullOrEmpty(header)) {
                return request.getRemoteAddr();
            }

            return Iterables.getLast(COMMA_SPLITTER.split(header));
        } else {
            return request.getRemoteAddr();
        }

    }

    private Set<Interval> getIntervals(final String[] trusted) {
        return Arrays.stream(trusted).filter(StringUtils::isNotEmpty).map(IpInterval::parse).collect(Collectors.toSet());
    }

    private boolean isTrusted(final Interval ipResource, final Set<Interval> ipRanges) {
        return ipRanges.stream().anyMatch(ipRange -> ipRange.getClass().equals(ipResource.getClass()) && ipRange.contains(ipResource));
    }
}
