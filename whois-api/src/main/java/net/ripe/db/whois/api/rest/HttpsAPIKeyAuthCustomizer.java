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
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

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

        if( !(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
        }

        final HttpServletRequest httpRequest = (HttpServletRequest) request;

        if(isvalidRequest(httpRequest)) {
            final HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.sendError(HttpStatus.BAD_REQUEST_400, "Bad Request");
            return;
        }

        if(!canProceed(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        LOGGER.debug("It is a api key request");

        if (RestServiceHelper.isHttpProtocol(httpRequest)){
            final HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.sendError(HttpStatus.UPGRADE_REQUIRED_426, "HTTPS required for Basic authorization");
            return;
        }

        try {
            final String accessKey = ApiKeyUtils.getAccessKey(httpRequest.getHeader(HttpHeaders.AUTHORIZATION));

            final String bearerToken = apiKeyAuthServiceClient.validateApiKey(httpRequest.getHeader(HttpHeaders.AUTHORIZATION), accessKey);
            chain.doFilter(new HttpApiAuthRequestWrapper((HttpServletRequest) request, accessKey, bearerToken), response);

        } catch (Exception ex) {
            final HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.sendError(HttpStatus.INTERNAL_SERVER_ERROR_500, "Api Key request failed");
        }
    }

    private static boolean isvalidRequest(HttpServletRequest httpRequest) {
        return (!StringUtils.isEmpty(httpRequest.getQueryString()) && httpRequest.getQueryString().contains(ApiKeyUtils.APIKEY_ACCESS_QUERY_PARAM)) ||
                (httpRequest.getHeader(HttpHeaders.AUTHORIZATION) != null && httpRequest.getHeader(HttpHeaders.AUTHORIZATION).startsWith("Bearer"));
    }

    private boolean canProceed(final HttpServletRequest httpRequest) {
        if(!isEnabled) {
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

        final private String bearerToken;
        final private String accessKey;

        private HttpApiAuthRequestWrapper(final HttpServletRequest request,  final String accessKey , final String bearerToken) {
            super(request);
            this.bearerToken = bearerToken;
            this.accessKey = accessKey;
        }

        @Override
        public String getQueryString() {
            final StringBuilder modifiedQueryString = (super.getQueryString() == null) ?
                    new StringBuilder() :
                    new StringBuilder(super.getQueryString()).append("&");

            return modifiedQueryString.append(ApiKeyUtils.APIKEY_ACCESS_QUERY_PARAM).append("=").append(accessKey).toString();
        }

        @Override
        public String getHeader(final String name) {
            if(!Objects.equals(name, HttpHeaders.AUTHORIZATION)) {
                return super.getHeader(name);
            }

            return "Bearer " +  bearerToken;
        }
    }
}
