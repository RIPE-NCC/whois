package net.ripe.db.whois.api.nrtm4;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

@Component
public class NrtmHttpControl implements Filter {

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (isHttps(httpRequest)){
            chain.doFilter(request, response);
        } else {
            final HttpServletResponse httpResponse = (HttpServletResponse)response;
            httpResponse.sendError(HttpStatus.UPGRADE_REQUIRED_426, "HTTPS required");
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    private boolean isHttps(HttpServletRequest request) {
        return Objects.equals(request.getHeader(HttpHeader.X_FORWARDED_PROTO.asString()).toLowerCase(),
                HttpScheme.HTTPS.asString());
    }
}
