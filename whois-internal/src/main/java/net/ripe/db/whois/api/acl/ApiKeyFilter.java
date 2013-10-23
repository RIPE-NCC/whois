package net.ripe.db.whois.api.acl;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import net.ripe.db.whois.api.abusec.JdbcApiKeyDao;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Component
public class ApiKeyFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyFilter.class);

    private final JdbcApiKeyDao apiKeyDao;

    @Autowired
    public ApiKeyFilter(final JdbcApiKeyDao apiKeyDao) {
        this.apiKeyDao = apiKeyDao;
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        final String offeredKey = request.getParameter("apiKey");

        if (StringUtils.isBlank(offeredKey)) {
            LOGGER.debug("Missing API key");
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "No apiKey parameter specified");
        } else {
            final Iterable<String> apiKeys = getApiKeys(offeredKey, (HttpServletRequest) request);
            if (Iterables.isEmpty(apiKeys)) {
                LOGGER.warn("Invalid API key received from {}: {} (should be {})", request.getRemoteAddr(), offeredKey, apiKeys);
                sendError(response, HttpServletResponse.SC_FORBIDDEN, "Invalid apiKey");
            } else {
                chain.doFilter(request, response);
            }
        }
    }

    private Iterable<String> getApiKeys(final String offeredKey, final HttpServletRequest request) {
        final String requestURI = request.getRequestURI();
        final List<String> uris = apiKeyDao.getUrisForApiKey(offeredKey);
        return Iterables.filter(uris, new Predicate<String>() {
            @Override
            public boolean apply(@Nullable String input) {
                return requestURI.startsWith(input);
            }
        });
    }

    private void sendError(final ServletResponse response, final int status, final String message) throws IOException {
        if (response instanceof HttpServletResponse) {
            final HttpServletResponse servletResponse = (HttpServletResponse) response;
            servletResponse.setStatus(status);
        }

        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.write(message);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    @Override
    public void destroy() {
    }
}
