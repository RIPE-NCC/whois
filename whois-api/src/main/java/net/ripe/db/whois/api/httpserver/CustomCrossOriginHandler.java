package net.ripe.db.whois.api.httpserver;

import com.google.common.net.HttpHeaders;
import jakarta.ws.rs.HttpMethod;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.CrossOriginHandler;
import org.eclipse.jetty.util.Callback;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 *  POST, PUT and OPTIONS (pre-flight requests) are allowed only if origin header is in whois.allow.cross.origin.hosts property
 *  Authenticated GET is allowed only if origin header is in whois.allow.cross.origin.hosts property, Access-Control-Allow-Credentials set to true
 *  UnAuthenticated GET requests are always allowed with Access-Control-Allow-Credentials set to false
 */
public class CustomCrossOriginHandler extends CrossOriginHandler {

    public CustomCrossOriginHandler(final String[] allowedHostsforCrossOrigin) {

        final Set<String> allowedOriginPatterns = getAllowedOriginPatterns(allowedHostsforCrossOrigin);
        if(!allowedOriginPatterns.isEmpty()) {
            setAllowedOriginPatterns(allowedOriginPatterns);
            setAllowCredentials(true);
        }
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {

       allowUnauthenticatedGetRequest(request, response);

       return super.handle(request, response, callback);
    }

    private void allowUnauthenticatedGetRequest(final Request request, final Response response) {
        final String origin = request.getHeaders().get(HttpHeaders.ORIGIN);

        if(StringUtils.isEmpty(origin) || !request.getMethod().equalsIgnoreCase(HttpMethod.GET)) {
            return;
        }

        if(getAllowedOriginPatterns().contains(origin) ) {
            //Authenticated GET requests are allowed
            return;
        }

        //Unauthenticated GET requests
        response.getHeaders().put(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.getHeaders().put(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "false");
    }

    private static Set<String> getAllowedOriginPatterns(final String[] allowedHostsforCrossOrigin) {
        return Stream.of(allowedHostsforCrossOrigin)
                .map(allowedHost -> String.format("https?://%s", allowedHost.replace(".", "\\.")))
                .collect(Collectors.toSet());
    }
}
