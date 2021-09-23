package net.ripe.db.whois.api.httpserver;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.net.HttpHeaders;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.Enumeration;

/**
 * X-Forwarded-For address will replace the request address.
 * In case of multiple (comma separated) ip address in X-Forwarded-For, rightmost will replace the request address.
 */
public class RemoteAddressCustomizer  implements HttpConfiguration.Customizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteAddressCustomizer.class);

    private static final Splitter COMMA_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();

    @Override
    public void customize(Connector connector, HttpConfiguration httpConfiguration, Request request) {
        request.setRemoteAddr(InetSocketAddress.createUnresolved(getRemoteAddress(request), request.getRemotePort()));
        LOGGER.debug("Received client ip is {}", request.getRemoteAddr());
    }

    @Nullable
    private String getRemoteAddress(final Request request) {
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