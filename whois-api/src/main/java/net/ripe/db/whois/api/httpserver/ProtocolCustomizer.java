    package net.ripe.db.whois.api.httpserver;

    import com.google.common.base.Splitter;
    import com.google.common.base.Strings;
    import com.google.common.collect.Iterables;
    import org.eclipse.jetty.http.HttpFields;
    import org.eclipse.jetty.http.HttpURI;
    import org.eclipse.jetty.server.HttpConfiguration;
    import org.eclipse.jetty.server.Request;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;

    import javax.annotation.Nullable;
    import java.util.Enumeration;

    import static org.eclipse.jetty.http.HttpHeader.X_FORWARDED_PROTO;

    /**
 * When HTTP requests are proxied via a loadbalancer, the X-Forwarded-Proto value will replace the request HTTP protocol (URL scheme).
 */
public class ProtocolCustomizer implements HttpConfiguration.Customizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolCustomizer.class);

    private static final Splitter COMMA_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();

    @Override
    public Request customize(final Request request, final HttpFields.Mutable mutable) {
        return new Request.Wrapper(request) {
            @Override
            public HttpURI getHttpURI() {
                return setScheme(request.getHttpURI(), getScheme(request));
            }
        };
    }

    private String getScheme(final Request request) {
        final String header = getLastHeaderValue(request, X_FORWARDED_PROTO.asString());
        if (Strings.isNullOrEmpty(header)) {
            return request.getHttpURI().getScheme();
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

    private static HttpURI setScheme(final HttpURI uri, final String scheme) {
        LOGGER.debug("Scheme is {} for {}", scheme, uri.asString());
        return HttpURI.build(uri).scheme(scheme).asImmutable();
    }
}
