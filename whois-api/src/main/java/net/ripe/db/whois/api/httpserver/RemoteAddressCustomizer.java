package net.ripe.db.whois.api.httpserver;

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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * When HTTP requests via trusted source use clientIp query paramater as remote Address if set
 */
public abstract class RemoteAddressCustomizer implements HttpConfiguration.Customizer {

    public static final String QUERY_PARAM_CLIENT_IP = "clientIp";
    private final Set<Interval> trusted;

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

        return customizeRemoteAddress(request);
    }

    abstract String customizeRemoteAddress(final Request request);

    private Set<Interval> getIntervals(final String[] trusted) {
        return Arrays.stream(trusted).map(IpInterval::parse).collect(Collectors.toSet());
    }

    private boolean isTrusted(final Interval ipResource, final Set<Interval> ipRanges) {
        return ipRanges.stream().anyMatch(ipRange -> ipRange.getClass().equals(ipResource.getClass()) && ipRange.contains(ipResource));
    }
}
