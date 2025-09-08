package net.ripe.db.whois.api.httpserver;

import com.google.common.net.HttpHeaders;
import jakarta.ws.rs.HttpMethod;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Arrays;

/**
 *
 *  POST, PUT and OPTIONS (pre-flight requests) are allowed only if origin header is in whois.allow.cross.origin.hosts property
 *  Authenticated GET is allowed only if origin header is in whois.allow.cross.origin.hosts property, Access-Control-Allow-Credentials set to true
 *  UnAuthenticated GET requests are always allowed with Access-Control-Allow-Credentials set to false
 */
public class CustomCrossOriginHandler extends Handler.Wrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomCrossOriginHandler.class);
    final protected String[] allowedHostsforCrossOrigin;

    public CustomCrossOriginHandler(@Value("${whois.allow.cross.origin.hosts}") final String[] allowedHostsforCrossOrigin) {
        this.allowedHostsforCrossOrigin = allowedHostsforCrossOrigin;
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {

        final String origin = request.getHeaders().get(HttpHeaders.ORIGIN);
        if(StringUtils.isEmpty(origin)) {
            return super.handle(request, response, callback);
        }

        final boolean isCredentialAllowed = isHostsAllowedForCrossOrigin(origin, allowedHostsforCrossOrigin);
        final String allowedOrigin = allowedOriginPatterns(request);

        if(!StringUtils.isEmpty(allowedOrigin)) {
            response.getHeaders().put(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, allowedOrigin);
            response.getHeaders().put(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, String.valueOf(isCredentialAllowed));
        }

       return super.handle(request, response, callback);
    }

    public static boolean isHostsAllowedForCrossOrigin(final String origin, final String[] allowedHostsforCrossOrigin) {
        try {
            final URI uri = new URI(origin);
            return Arrays.stream(allowedHostsforCrossOrigin).anyMatch(host -> host.equalsIgnoreCase(uri.getHost()));
        } catch (Exception e) {
            LOGGER.debug("Failed to parse origin header", e);
            return false;
        }
    }

    @Nullable
    private String allowedOriginPatterns(final Request request) {

        final String origin = request.getHeaders().get(HttpHeaders.ORIGIN);
        final String method = request.getMethod();

        if(isHostsAllowedForCrossOrigin(origin, allowedHostsforCrossOrigin)) {
            return origin;
        }

        if (method.equals(HttpMethod.GET) || method.equals(HttpMethod.HEAD)) {
            return "*";
        }

        return null;
    }
}
