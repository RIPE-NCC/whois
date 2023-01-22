package net.ripe.db.whois.api.httpserver;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.rewrite.handler.Rule;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

public class HttpTransportRule extends Rule {

    private final HttpScheme transport;
    private final Rule delegate;

    public HttpTransportRule(final HttpScheme transport, final Rule delegate) {
        this.transport = transport;
        this.delegate = delegate;
    }

    @Override
    public String matchAndApply(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
        return hasTransport(request)? delegate.matchAndApply(target, request, response) : null;
    }


    private boolean hasTransport(final HttpServletRequest request) {
        final Enumeration<String> header = request.getHeaders(HttpHeader.X_FORWARDED_PROTO.toString());
        if (header == null || !header.hasMoreElements()) {
            return false;
        }

        return transport.is(header.nextElement());
    }

    @Override
    public boolean isTerminating() {
        return delegate.isTerminating();
    }

    @Override
    public boolean isHandling() {
        return delegate.isHandling();
    }

}
