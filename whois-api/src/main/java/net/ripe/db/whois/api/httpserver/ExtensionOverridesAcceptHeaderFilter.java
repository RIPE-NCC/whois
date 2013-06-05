package net.ripe.db.whois.api.httpserver;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

@Component
public class ExtensionOverridesAcceptHeaderFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionOverridesAcceptHeaderFilter.class);

    private static final Map<String, String> EXTENSION_TO_ACCEPTS = ImmutableMap.of(
            "xml", "application/xml",
            "json", "application/json"
    );

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            chain.doFilter(OverrideAcceptHeaderWrapper.wrapRequest((HttpServletRequest) request), response);
        } else {
            LOGGER.warn("Unexpected request: {}", request);
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
    }

    private static class OverrideAcceptHeaderWrapper extends HttpServletRequestWrapper {

        private final String requestURI;
        private final StringBuffer requestURL;
        private final String accepts;

        public static ServletRequest wrapRequest(HttpServletRequest request) {
            String requestURI = request.getRequestURI();
            final int lastDot = requestURI.lastIndexOf('.');
            String accepts = EXTENSION_TO_ACCEPTS.get(requestURI.substring(lastDot + 1));
            if (accepts == null) {
                return request;
            }

            requestURI = requestURI.substring(0, lastDot);

            StringBuffer requestURL = request.getRequestURL();
            requestURL = requestURL.delete(requestURL.length() - (accepts.length() + 1), requestURL.length());

            return new OverrideAcceptHeaderWrapper(request, requestURI, requestURL, accepts);
        }

        private OverrideAcceptHeaderWrapper(final HttpServletRequest request, final String requestURI, final StringBuffer requestURL, final String accepts) {
            super(request);
            this.requestURI = requestURI;
            this.requestURL = requestURL;
            this.accepts = accepts;
        }

        @Override
        public String getHeader(String name) {
            if (name.equalsIgnoreCase("Accept")) {
                return accepts;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (name.equalsIgnoreCase("Accept")) {
                return new SingletonEnumeration<>(accepts);
            }
            return super.getHeaders(name);
        }

        @Override
        public String getRequestURI() {
            return requestURI;
        }

        @Override
        public StringBuffer getRequestURL() {
            return requestURL;
        }
    }

    static class SingletonEnumeration<E> implements Enumeration<E> {
        E value;

        public SingletonEnumeration(E value) {
            this.value = value;
        }

        @Override
        public boolean hasMoreElements() {
            return (value != null);
        }

        @Override
        public E nextElement() {
            E res = value;
            value = null;
            return res;
        }
    }
}
