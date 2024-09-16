package net.ripe.db.whois.api.httpserver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.rewrite.handler.Rule;

import java.io.IOException;

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
        return transport.is(request.getScheme());
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
