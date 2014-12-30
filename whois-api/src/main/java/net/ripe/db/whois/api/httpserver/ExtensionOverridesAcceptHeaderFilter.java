package net.ripe.db.whois.api.httpserver;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

@Component
public class ExtensionOverridesAcceptHeaderFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionOverridesAcceptHeaderFilter.class);

    private static final Map<String, String> EXTENSION_TO_MEDIA_TYPE = ImmutableMap.of(
            "xml", MediaType.APPLICATION_XML,
            "json", MediaType.APPLICATION_JSON
    );

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        // do nothing
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
        // do nothing
    }

    private static final class OverrideAcceptHeaderWrapper extends HttpServletRequestWrapper {

        private final String requestURI;
        private final StringBuffer requestURL;
        private final String mediaType;

        public static ServletRequest wrapRequest(HttpServletRequest request) {
            final String extension = getExtension(request.getRequestURI());
            if (extension == null) {
                return request;
            }

            final String mediaType = EXTENSION_TO_MEDIA_TYPE.get(extension);
            if (mediaType == null) {
                return request;
            }

            StringBuffer requestURL = request.getRequestURL();
            requestURL = requestURL.delete((requestURL.length() - extension.length() - 1), requestURL.length());

            String requestURI = request.getRequestURI();
            requestURI = requestURI.substring(0, requestURI.length() - extension.length() - 1);

            return new OverrideAcceptHeaderWrapper(request, requestURI, requestURL, mediaType);
        }

        private OverrideAcceptHeaderWrapper(final HttpServletRequest request, final String requestURI, final StringBuffer requestURL, final String mediaType) {
            super(request);
            this.requestURI = requestURI;
            this.requestURL = requestURL;
            this.mediaType = mediaType;
        }

        @Nullable
        private static String getExtension(final String uri) {
            final int lastDot = uri.lastIndexOf('.');
            if (lastDot == -1) {
                return null;
            } else {
                return uri.substring(lastDot + 1);
            }
        }

        @Override
        public String getHeader(String name) {
            if (name.equalsIgnoreCase(HttpHeaders.ACCEPT)) {
                return mediaType;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (name.equalsIgnoreCase(HttpHeaders.ACCEPT)) {
                return new SingletonEnumeration<>(mediaType);
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
