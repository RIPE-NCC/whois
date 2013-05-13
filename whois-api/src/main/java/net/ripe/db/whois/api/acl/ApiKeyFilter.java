package net.ripe.db.whois.api.acl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class ApiKeyFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyFilter.class);

    private String apiKey;

    @Value("${api.key}")
    public void setApiKey(final String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        final String offeredKey = request.getParameter("apiKey");
        if (offeredKey == null) {
            LOGGER.debug("Missing API key");
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "No apiKey parameter specified");
        } else if (!offeredKey.equals(apiKey)) {
            LOGGER.warn("Invalid API key received from {}: {}", request.getRemoteAddr(), offeredKey);
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "Invalid apiKey");
        } else {
            chain.doFilter(request, response);
        }
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
