package net.ripe.db.whois.api.rest;

import com.google.common.net.HttpHeaders;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import net.ripe.db.whois.common.oauth.OAuthUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;
import static jakarta.servlet.http.HttpServletRequest.BASIC_AUTH;

@Component
public class HttpsBasicAuthFiler implements Filter {

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {

        if(!canProceed(request)) {
            chain.doFilter(request, response);
            return;
        }

        chain.doFilter(new HttpBasicAuthRequestWrapper((HttpServletRequest) request), response);
    }

    private boolean canProceed(final ServletRequest request) {
       if(! (request instanceof HttpServletRequest httpRequest)) {
           return false;
       }

        return RestServiceHelper.isBasicAuth(httpRequest)
               && !OAuthUtils.isAPIKeyRequest(httpRequest.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @Override
    public void destroy() {
        // do nothing
    }

    private static final class HttpBasicAuthRequestWrapper extends HttpServletRequestWrapper {

        private HttpBasicAuthRequestWrapper(final HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getQueryString() {
            final String basicAuthPassword = getBasicAuthPassword(getHeader(HttpHeaders.AUTHORIZATION));
            if(basicAuthPassword == null) {
                return super.getQueryString();
            }

            final StringBuilder modifiedQueryString = (super.getQueryString() == null) ?
                                                             new StringBuilder() :
                                                             new StringBuilder(super.getQueryString()).append("&");

            return modifiedQueryString.append("password=").append(basicAuthPassword).toString();
        }
    }

    private static String getBasicAuthPassword(final String authHeader){
        final String base64Credentials = authHeader.substring(BASIC_AUTH.length()).trim();
        final byte[] credDecoded = new Base64().decode(base64Credentials);

        final String usernameWithPassword = new String(credDecoded, StandardCharsets.ISO_8859_1);

        return usernameWithPassword.contains(":") ?  StringUtils.substringAfter(usernameWithPassword, ":") : null;
    }
}
