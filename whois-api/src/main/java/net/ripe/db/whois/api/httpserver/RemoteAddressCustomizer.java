package net.ripe.db.whois.api.httpserver;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.net.HttpHeaders;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.ip.Interval;
import net.ripe.db.whois.common.ip.IpInterval;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.ConnectionMetaData;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URLDecoder;
import java.util.List;

import static org.eclipse.jetty.http.HttpHeader.X_FORWARDED_PROTO;

/**
 * When HTTP requests via trusted source use clientIp query paramater as remote Address if set
 */
public class RemoteAddressCustomizer implements HttpConfiguration.Customizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteAddressCustomizer.class);

    private static final Splitter COMMA_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();
    public static final String QUERY_PARAM_CLIENT_IP = "clientIp";

    // trusted (internal) IP addresses are allowed to use clientIp
    private final IpRanges ipRanges;

    // if client address is set in X-Forwarded-For header by HTTP proxy
    private final boolean usingForwardedForHeader;

    public RemoteAddressCustomizer(final IpRanges ipRanges, final boolean xForwardedFor) {
        this.ipRanges = ipRanges;
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
                        final String remoteAddress = getEvaluatedRemoteAddr(request);

                        return InetSocketAddress.createUnresolved(remoteAddress, Request.getRemotePort(request));
                    }

                    @Override
                    public boolean isSecure() {
                        return HttpScheme.HTTPS.name().equalsIgnoreCase(getScheme());
                    }
                };
            }

            private String getEvaluatedRemoteAddr(final Request request) {
                final String remoteAddress = stripBrackets(getRemoteAddrFromRequest(request));

                if(!ipRanges.isTrusted(getInterval(remoteAddress)))  return remoteAddress;

                final String clientIp =  getQueryParamValue(request, QUERY_PARAM_CLIENT_IP);
                if(StringUtils.isEmpty(clientIp)) {
                   return remoteAddress;
                }

                try {
                    final String decodedClientIp = URLDecoder.decode(clientIp);
                    return InetAddresses.isInetAddress(clientIp) ? decodedClientIp : remoteAddress;
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Invalid client IP address: {}, falling back to remote address", clientIp);
                    return remoteAddress;
                }
            }

            public String getScheme() {
                final String header = getLastHeaderValue(request, X_FORWARDED_PROTO.asString());

                return StringUtils.isEmpty(header) ? request.getHttpURI().getScheme() : header;
            }

            private String getRemoteAddrFromRequest(final Request request){
                if (!usingForwardedForHeader) {
                    return Request.getRemoteAddr(request);
                }

                final String xForwardedFor = getLastHeaderValue(request, HttpHeaders.X_FORWARDED_FOR);
                return StringUtils.isEmpty(xForwardedFor) ? Request.getRemoteAddr(request) : xForwardedFor;
            }

            @Nullable
            private String getLastHeaderValue(final Request request, final String headerName) {
                final List<String> headers = request.getHeaders().getValuesList(headerName);
                if(headers.isEmpty()) return null;

               return Iterables.getLast(COMMA_SPLITTER.split(headers.getLast()), null);
            }

            @Nullable
            private String getQueryParamValue(final Request request, final String paramName) {
               try {
                     return Request.extractQueryParameters(request).getValue(paramName);
                }  catch (Exception e) {
                    LOGGER.warn("{} on query parameter {}: {}", e.getClass().getName(), paramName, e.getMessage());
                    return null;
                }
            }
        };
    };

    private Interval getInterval(final String address){
        return IpInterval.parse(address);
    }

    private static String stripBrackets(final String address) {
        return (address.startsWith("[") && address.endsWith("]")) ? address.substring(1, address.length() - 1) : address;
    }
}
