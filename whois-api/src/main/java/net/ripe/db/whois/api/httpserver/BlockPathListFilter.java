package net.ripe.db.whois.api.httpserver;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class BlockPathListFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockPathListFilter.class);

    private final List<BlockedPath> blockedPaths;

    @Autowired
    public BlockPathListFilter(@Value("${whois.api.blocked.paths:}") final String[] blockedPaths) {

        this.blockedPaths = Arrays.stream(blockedPaths)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(BlockedPath::new)
                .toList();
    }

    @Override
    public void doFilter(
            final ServletRequest request,
            final ServletResponse response,
            final FilterChain filterChain)
            throws IOException, ServletException {

        if (blockedPaths.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (request instanceof HttpServletRequest httpRequest &&
                response instanceof HttpServletResponse httpResponse) {

            final String path = httpRequest.getRequestURI();

            if (blockedPaths.stream()
                    .anyMatch(blockedPath -> blockedPath.matches(httpRequest))) {

                LOGGER.debug("Blocked path: {}{}", path,
                        httpRequest.getQueryString() != null
                                ? "?" + httpRequest.getQueryString()
                                : "");

                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpResponse.getWriter().write("Request not allowed for policy reasons");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private static class BlockedPath {

        private final String path;
        private final String queryString;

        BlockedPath(final String value) {
            final UriComponents uri = UriComponentsBuilder.fromUriString(value.startsWith("/") ? value : "/" + value).build();

            this.path = uri.getPath();
            this.queryString = uri.getQuery();
        }

        boolean matches(final HttpServletRequest request) {

            final String requestPath = request.getRequestURI();
            final String requestQuery = request.getQueryString();

            if (StringUtils.isEmpty(requestQuery)) {
                return requestPath.contains(path);
            }

            // If queryParam exists in the request, then it must match full path
            if(!path.equals(requestPath)) {
                return false;
            }

            //Request param can contain in any order
            return requestQuery.contains(queryString);
        }
    }
}