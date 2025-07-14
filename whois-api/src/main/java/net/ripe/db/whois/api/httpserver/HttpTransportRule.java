package net.ripe.db.whois.api.httpserver;

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
    public Handler matchAndApply(Handler handler) throws IOException {
        return hasTransport(handler)? delegate.matchAndApply(handler) : null;
    }

    private boolean hasTransport(final Handler handler) {
        return transport.is(handler.getHttpURI().getScheme());
    }

    @Override
    public boolean isTerminating() {
        return delegate.isTerminating();
    }

}
