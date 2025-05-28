package net.ripe.db.whois.api.httpserver;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.net.HttpHeaders;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.common.ip.Interval;
import net.ripe.db.whois.common.ip.IpInterval;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.BadMessageException;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.Request;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * When HTTP requests via trusted source use clientIp query paramater as remote Address if set
 */
public class RemoteAddressCustomizer implements HttpConfiguration.Customizer {

    private static final Splitter COMMA_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();
    public static final String QUERY_PARAM_CLIENT_IP = "clientIp";
    private final Set<Interval> trusted;

    // if client address is set in X-Forwarded-For header by HTTP proxy
    private final boolean usingForwardedForHeader;

    public RemoteAddressCustomizer(final String trustedIpRanges, final boolean xForwardedFor) {
        this.trusted = getIntervals(COMMA_SPLITTER.split(trustedIpRanges));
        this.usingForwardedForHeader = xForwardedFor;
    }

    @Override
    public void customize(final Connector connector, final HttpConfiguration httpConfiguration, final Request request) {
        String remoteAddress = stripSquareBrackets(getRemoteAddrFromRequest(request));

        if (isTrusted(remoteAddress)){
            String clientIp = getClientIp(request);
            if (clientIp != null){
                remoteAddress = clientIp;
            }
        }
        request.setRemoteAddr(InetSocketAddress.createUnresolved(remoteAddress, request.getRemotePort()));
    }

    @Nullable
    private String getClientIp(final Request request){
        try {
            final String clientIp = request.getParameter(QUERY_PARAM_CLIENT_IP);
            if (StringUtils.isNotEmpty(clientIp)) {
                return clientIp;
            }
        } catch (BadMessageException ex){
            //ignore
        }
        return null;
    }

    private String stripSquareBrackets(final String address){
        return (address.startsWith("[") && address.endsWith("]")) ? address.substring(1, address.length() - 1) :
                address;
    }

    private String getRemoteAddrFromRequest(final Request request){
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
        }

        return request.getRemoteAddr();
    }


    private boolean isTrusted(final String remoteAddress){
        return isTrusted(getInterval(remoteAddress));
    }

    private boolean isTrusted(final Interval ipResource) {
        return trusted.stream().anyMatch(ipRange -> ipRange.getClass().equals(ipResource.getClass()) && ipRange.contains(ipResource));
    }
    private Interval getInterval(final String address){
        return IpInterval.asIpInterval(InetAddresses.forString(address));
    }

    private Set<Interval> getIntervals(final Iterable<String> trusted) {
        final Set<Interval> intervals = new HashSet<>();

        for (final String ip : trusted) {
            if (StringUtils.isNotEmpty(ip)) {
                intervals.add(IpInterval.parse(ip));
            }
        }

        return intervals;
    }


}
