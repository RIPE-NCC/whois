    package net.ripe.db.whois.api.httpserver;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.net.HttpHeaders;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;

/**
 * When HTTP requests are proxied via a loadbalancer, the X-Forwarded-Proto value will replace the request HTTP protocol (URL scheme).
 */
public class ProtocolCustomizer implements HttpConfiguration.Customizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolCustomizer.class);

    private static final Splitter COMMA_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();

    @Override
    public void customize(final Connector connector, final HttpConfiguration channelConfig, final Request request) {
        request.setHttpURI(setScheme(request.getHttpURI(), getScheme(request)));
    }

    private String getScheme(final Request request) {
        final Enumeration<String> headers = request.getHeaders(HttpHeaders.X_FORWARDED_PROTO);
        if (headers == null || !headers.hasMoreElements()) {
            return request.getScheme();
        }

        final String header = headers.nextElement();
        if (Strings.isNullOrEmpty(header)) {
            return request.getScheme();
        }

        return Iterables.getLast(COMMA_SPLITTER.split(header));
    }

    private static HttpURI setScheme(final HttpURI uri, final String scheme) {
        LOGGER.debug("Scheme is {} for {}", scheme, uri.asString());
        return HttpURI.build(uri).scheme(scheme).asImmutable();
    }
}
