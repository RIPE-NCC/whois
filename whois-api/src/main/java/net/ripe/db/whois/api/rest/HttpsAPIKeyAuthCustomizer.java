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
import net.ripe.db.whois.common.apiKey.ApiKeyAuthServiceClient;
import net.ripe.db.whois.common.apiKey.ApiKeyUtils;
import net.ripe.db.whois.common.apiKey.OAuthSession;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static jakarta.servlet.http.HttpServletRequest.BASIC_AUTH;

@Component
public class HttpsAPIKeyAuthCustomizer implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpsAPIKeyAuthCustomizer.class);

    final private boolean isEnabled;
    final ApiKeyAuthServiceClient apiKeyAuthServiceClient;

    @Autowired
    public HttpsAPIKeyAuthCustomizer(@Value("${apikey.authenticate.enabled:false}") final boolean isEnabled,
                                     final ApiKeyAuthServiceClient apiKeyAuthServiceClient) {
        this.apiKeyAuthServiceClient = apiKeyAuthServiceClient;
        this.isEnabled = isEnabled;
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {

        if(!canProceed(request)) {
            chain.doFilter(request, response);
            return;
        }

        LOGGER.info("It is a api key request");
        final HttpServletRequest httpRequest = (HttpServletRequest) request;

        if (RestServiceHelper.isHttpProtocol(httpRequest)){
            final HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.sendError(HttpStatus.UPGRADE_REQUIRED_426, "HTTPS required for Basic authorization");
            return;
        }

        if(!StringUtils.isBlank(httpRequest.getQueryString()) && httpRequest.getQueryString().contains(ApiKeyUtils.APIKEY_QUERY_PARAM)) {
            final HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.sendError(HttpStatus.BAD_REQUEST_400, "Bad Request");
            return;
        }

        try {
            final String oAuthSession = apiKeyAuthServiceClient.validateApiKey(httpRequest.getHeader(HttpHeaders.AUTHORIZATION));
            chain.doFilter(new HttpApiAuthRequestWrapper((HttpServletRequest) request, oAuthSession), response);

        } catch (Exception ex) {
            final HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.sendError(HttpStatus.INTERNAL_SERVER_ERROR_500, "Api Key request failed");
        }
    }

    private boolean canProceed(final ServletRequest request) {
        if(!isEnabled || !(request instanceof HttpServletRequest httpRequest)) {
            return false;
        }

       //TODO: Remove this logic when basic auth support is deprecated
       return ApiKeyUtils.isAPIKeyRequest(httpRequest.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @Override
    public void destroy() {
        // do nothing
    }

    private static final class HttpApiAuthRequestWrapper extends HttpServletRequestWrapper {

        final private String oAuthSession;

        private HttpApiAuthRequestWrapper(final HttpServletRequest request, final String oAuthSession) {
            super(request);
            this.oAuthSession = oAuthSession;
        }

        @Override
        public String getQueryString() {
            final StringBuilder modifiedQueryString = (super.getQueryString() == null) ?
                    new StringBuilder() :
                    new StringBuilder(super.getQueryString()).append("&");

            return modifiedQueryString.append(ApiKeyUtils.APIKEY_QUERY_PARAM).append("=").append(oAuthSession).toString();
        }
    }

    private static String getBasicAuthUsername(final HttpServletRequest request){
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(authHeader == null || !authHeader.startsWith("Basic ")) {
            return null;
        }

        final String base64Credentials = authHeader.substring(BASIC_AUTH.length()).trim();
        final byte[] credDecoded = new Base64().decode(base64Credentials);

        final String usernameWithPassword = new String(credDecoded, StandardCharsets.ISO_8859_1);

        return usernameWithPassword.contains(":") ?  StringUtils.substringBefore(usernameWithPassword, ":") : null;
    }
}
