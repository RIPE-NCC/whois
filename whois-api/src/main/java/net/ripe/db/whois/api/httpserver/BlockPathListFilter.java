package net.ripe.db.whois.api.httpserver;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BlockPathListFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockPathListFilter.class);

    private final Set<String> blockedPaths;

    @Autowired
    public BlockPathListFilter(@Value("${whois.api.blocked.paths:}") final String[] blockedPaths ) {
        this.blockedPaths = Arrays.stream(blockedPaths)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain) throws IOException, ServletException {

        if(blockedPaths.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (request instanceof HttpServletRequest httpRequest &&
                response instanceof HttpServletResponse httpResponse) {

            final String path = httpRequest.getRequestURI();

            if (blockedPaths.contains(path)) {
                LOGGER.debug("Blocked path: {}", path);
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpResponse.getWriter().write("Request not allowed for policy reasons");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
