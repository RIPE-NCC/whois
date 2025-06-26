package net.ripe.db.whois.api.httpserver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.rewrite.handler.Rule;

import java.io.IOException;
import java.util.Set;

/**
 * {@link Rule} to conditionally match on HTTP request method before applying a rewrite rule.
 */
public class HttpMethodRule extends Rule {

    private final Set<HttpMethod> methods;
    private final Rule delegate;

    /**
     * Constructor.
     * @param method the HTTP method the request should have
     * @param rule the actual rewrite rule
     */
    public HttpMethodRule(final HttpMethod method, final Rule rule) {
        this(Set.of(method), rule);
    }

    public HttpMethodRule(final Set<HttpMethod> methods, final Rule rule) {
        this.methods = methods;
        this.delegate = rule;
    }

    @Override
    public String matchAndApply(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
        return methods.contains(HttpMethod.valueOf(request.getMethod()))?
                delegate.matchAndApply(target, request, response) :
                null;
        } catch (IllegalArgumentException iae) {
            return null;
        }
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
