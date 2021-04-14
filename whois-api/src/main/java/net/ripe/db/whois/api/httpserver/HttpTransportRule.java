package net.ripe.db.whois.api.httpserver;

import org.eclipse.jetty.rewrite.handler.Rule;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

public class HttpTransportRule extends Rule {

    private final String transport;
    private final Rule delegate;

    public HttpTransportRule(final String transport, final Rule delegate) {
        this.transport = transport;
        this.delegate = delegate;
    }

    @Override
    public String matchAndApply(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
        return hasTransport(request)? delegate.matchAndApply(target, request, response) : null;
    }


    private boolean hasTransport(final HttpServletRequest request) {
        final Enumeration<String> header = request.getHeaders("X-Forwarded-Proto");
        if (header == null || !header.hasMoreElements()) {
            return false;
        }

        return header.nextElement().equalsIgnoreCase(transport);
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
