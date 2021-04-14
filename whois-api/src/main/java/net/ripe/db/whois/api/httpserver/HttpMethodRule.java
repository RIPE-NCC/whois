package net.ripe.db.whois.api.httpserver;

import org.eclipse.jetty.rewrite.handler.Rule;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link Rule} to conditionally match on HTTP request method before applying a rewrite rule.
 */
public class HttpMethodRule extends Rule {

    private final Set<String> methods;
    private final Rule delegate;

    /**
     * Ctor.
     * @param method the HTTP method the request should have
     * @param rule the actual rewrite rule
     */
    public HttpMethodRule(final String method, final Rule rule) {
        this(Set.of(method), rule);
    }

    public HttpMethodRule(final Set<String> methods, final Rule rule) {
        this.methods = methods.stream().map(String::toLowerCase).collect(Collectors.toSet());
        this.delegate = rule;
    }

    @Override
    public String matchAndApply(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
        return methods.contains(request.getMethod().toLowerCase())?
                delegate.matchAndApply(target, request, response) :
                null;
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
