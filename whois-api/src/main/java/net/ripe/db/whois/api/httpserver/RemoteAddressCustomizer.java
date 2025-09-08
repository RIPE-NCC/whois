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
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.ConnectionMetaData;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.util.UrlEncoded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import static org.eclipse.jetty.http.HttpHeader.X_FORWARDED_PROTO;

/**
 * When HTTP requests via trusted source use clientIp query paramater as remote Address if set
 */
public class RemoteAddressCustomizer implements HttpConfiguration.Customizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteAddressCustomizer.class);

    private static final Splitter COMMA_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();
    public static final String QUERY_PARAM_CLIENT_IP = "clientIp";

    // trusted (internal) IP addresses are allowed to use clientIp
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
                        return InetSocketAddress.createUnresolved(URLDecoder.decode(getHost()), Request.getRemotePort(request));
                    }

                    @Override
                    public boolean isSecure() {
                        return HttpScheme.HTTPS.name().equalsIgnoreCase(getScheme());
                    }

                    private String getHost(){
                        final String remoteAddress = stripBrackets(getRemoteAddrFromRequest(request));
                        if (isURIValidEncoded()){
                            final String clientIp = getClientIp(request);

                            if (isTrusted(remoteAddress) && StringUtils.isNotEmpty(clientIp)){
                                return clientIp;
                            }
                        }
                        return remoteAddress;
                    }

                    private boolean isURIValidEncoded(){
                        try {
                            UrlEncoded.decodeQuery(request.getHttpURI().getQuery());
                            return true;
                        } catch (Exception e) {
                            return false;
                        }
                    }
                };
            }

            private String getClientIp(final Request request) {
                final String clientIp =  getQueryParamValue(request, QUERY_PARAM_CLIENT_IP);
                if(StringUtils.isEmpty(clientIp)) {
                   return null;
                }

                try {
                   InetAddresses.forString(URLDecoder.decode(clientIp));
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Invalid client IP address: {}, falling back to remote address", clientIp);
                    return null;
                }

                return clientIp;
            }

            public String getScheme() {
                final String header = getLastHeaderValue(request, X_FORWARDED_PROTO.asString());
                if (Strings.isNullOrEmpty(header)) {
                    return request.getHttpURI().getScheme();
                }
                return header;
            }

            private String getRemoteAddrFromRequest(final Request request){
                if (!usingForwardedForHeader) {
                    return Request.getRemoteAddr(request);
                }

                final String xForwardedFor = getLastHeaderValue(request, HttpHeaders.X_FORWARDED_FOR);
                return Strings.isNullOrEmpty(xForwardedFor) ? Request.getRemoteAddr(request) : xForwardedFor;
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
                try {
                    for (Fields.Field queryParameter : Request.extractQueryParameters(request)) {
                        if (queryParameter.getName().equals(paramName)) {
                            return queryParameter.getValue();
                        }
                    }
                }  catch (BadMessageException e) {
                    LOGGER.debug("{} on query parameter {}: {}", e.getClass().getName(), paramName, e.getMessage());
                }
                return null;
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

    private static String stripBrackets(final String address) {
        return (address.startsWith("[") && address.endsWith("]")) ? address.substring(1, address.length() - 1) : address;
    }
}
