package net.ripe.db.whois.api.httpserver;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.net.HttpHeaders;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.common.ip.Interval;
import net.ripe.db.whois.common.ip.IpInterval;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.ConnectionMetaData;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.Request;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.eclipse.jetty.http.HttpHeader.X_FORWARDED_FOR;
import static org.eclipse.jetty.http.HttpHeader.X_FORWARDED_PROTO;

/**
 * When HTTP requests via trusted source use clientIp query paramater as remote Address if set
 */
public class RemoteAddressCustomizer implements HttpConfiguration.Customizer {

    private static final Splitter COMMA_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();
    private static final Splitter AMPERSAND_SPLITTER = Splitter.on('&').omitEmptyStrings().trimResults();
    private static final Splitter EQUALS_SPLITTER = Splitter.on('=').omitEmptyStrings().trimResults();

    public static final String QUERY_PARAM_CLIENT_IP = "clientIp";
    private final Set<Interval> trusted;

    // if client address is set in X-Forwarded-For header by HTTP proxy
    private final boolean usingForwardedForHeader;

    public RemoteAddressCustomizer(final String trustedIpRanges, final boolean xForwardedFor) {
        this.trusted = getIntervals(COMMA_SPLITTER.split(trustedIpRanges));
        this.usingForwardedForHeader = xForwardedFor;
    }

    @Override
    public Request customize(final Request request, final HttpFields.Mutable responseHeaders) {
        return new Request.Wrapper(request) {
            @Override
            public HttpURI getHttpURI() {
                return HttpURI.build(request.getHttpURI()).scheme(getScheme()).asImmutable();
            }

            @Override
            public ConnectionMetaData getConnectionMetaData() {
                return new ConnectionMetaData.Wrapper(request.getConnectionMetaData()) {
                    @Override
                    public SocketAddress getRemoteSocketAddress() {
                        String remoteAddress = stripSquareBrackets(getRemoteAddrFromRequest(request));
                        if (isTrusted(remoteAddress)){
                            String clientIp = getClientIp(request);
                            if (clientIp != null){
                                remoteAddress = clientIp;
                            }
                        }
                        return InetSocketAddress.createUnresolved(remoteAddress, Request.getRemotePort(request));
                    }

                    @Override
                    public boolean isSecure() {
                        return HttpScheme.HTTPS.name().equalsIgnoreCase(getScheme());
                    }
                };
            }

            @Nullable
            private String getClientIp(final Request request) {
                final String clientIp = getQueryParamValue(request, QUERY_PARAM_CLIENT_IP);
                if (StringUtils.isNotEmpty(clientIp)) {
                    return clientIp;
                }
                return null;
            }

            private String stripSquareBrackets(final String address){
                return (address.startsWith("[") && address.endsWith("]")) ? address.substring(1, address.length() - 1) : address;
            }

            public String getScheme() {
                final String header = getLastHeaderValue(request, X_FORWARDED_PROTO.asString());
                if (Strings.isNullOrEmpty(header)) {
                    return request.getHttpURI().getScheme();
                }
                return header;
            }

            private String getXForwardedForAddress(final Request request) {
                final String header = getLastHeaderValue(request, X_FORWARDED_FOR.asString());
                if (Strings.isNullOrEmpty(header)) {
                    return Request.getRemoteAddr(request);
                }
                return header;
            }

            @Nullable
            private String getLastHeaderValue(final Request request, final String headerName) {
                final Enumeration<String> headers = request.getHeaders().getValues(headerName);
                while (headers.hasMoreElements()) {
                    final String next = headers.nextElement();
                    if (!headers.hasMoreElements() && !Strings.isNullOrEmpty(next)) {
                        return Iterables.getLast(COMMA_SPLITTER.split(next));
                    }
                }
                return null;
            }

            @Nullable
            private String getQueryParamValue(final Request request, final String paramName) {
                if(request.getHttpURI().getQuery() == null) return null;

                for (String queryParam : AMPERSAND_SPLITTER.split(request.getHttpURI().getQuery())) {
                    final Iterator<String> split = EQUALS_SPLITTER.split(queryParam).iterator();
                    if (split.hasNext()) {
                        if (split.next().equals(paramName)) {
                            if (split.hasNext()) {
                                return split.next();
                            }
                        }
                    }
                }
                return null;
            }

            private String getRemoteAddrFromRequest(final Request request){
                if (usingForwardedForHeader){
                    final String xForwardedFor = getLastHeaderValue(request, HttpHeaders.X_FORWARDED_FOR);
                    return (Strings.isNullOrEmpty(xForwardedFor) ? Request.getRemoteAddr(request) : xForwardedFor);
                } else {
                    return request.getConnectionMetaData().getRemoteSocketAddress().toString();
                }
            }

        };
    };

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
