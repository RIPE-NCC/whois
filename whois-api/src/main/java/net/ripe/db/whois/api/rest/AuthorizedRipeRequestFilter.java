package net.ripe.db.whois.api.rest;

import com.google.common.collect.Sets;
import io.netty.util.internal.StringUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.ripe.db.whois.common.apiKey.ApiKeyUtils;
import net.ripe.db.whois.common.sso.AuthServiceClient;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class AuthorizedRipeRequestFilter implements Filter {

    private final Set<String> authorisedSites;

    AuthorizedRipeRequestFilter(@Value("${authorised.ripe.sites:}") final String authorisedSites){
        this.authorisedSites = StringUtil.isNullOrEmpty(authorisedSites) ? Sets.newHashSet() :
                Stream.of(authorisedSites.split(",")).collect(Collectors.toSet());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (!isAuthorisedRipeSite(httpRequest) && (isAuthenticated(httpRequest) || isUpdateRequest(httpRequest))){
            final HttpServletResponse httpResponse = (HttpServletResponse)response;
            httpResponse.sendError(HttpStatus.FORBIDDEN_403, "Not allowed to do this operation");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isAuthorisedRipeSite(final HttpServletRequest httpRequest){
        final String remoteHost = httpRequest.getRemoteHost();
        if (!remoteHost.endsWith(".ripe.net")){
            return true;
        }
        return authorisedSites.contains(remoteHost);
    }

    private boolean isAuthenticated(final HttpServletRequest httpRequest){
        final String authHeader = httpRequest.getHeader(HttpHeader.AUTHORIZATION.name());
        return httpRequest.getParameterMap().containsKey("password") ||
                httpRequest.getParameterMap().containsKey(ApiKeyUtils.APIKEY_KEY_ID_QUERY_PARAM) ||
                httpRequest.getParameterMap().containsKey(AuthServiceClient.TOKEN_KEY) ||
                !StringUtil.isNullOrEmpty(authHeader);
    }

    private boolean isUpdateRequest(final HttpServletRequest httpRequest){
        final String method = httpRequest.getMethod();
        return HttpMethod.POST.name().equalsIgnoreCase(method) || HttpMethod.PUT.name().equalsIgnoreCase(method);
    }
}
