package net.ripe.db.whois.api.httpserver;

import com.google.common.collect.Sets;
import com.google.common.net.HttpHeaders;
import jakarta.ws.rs.HttpMethod;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.CrossOriginHandler;
import org.eclipse.jetty.util.Callback;

/**
 *
 *  POST, PUT and OPTIONS (pre-flight requests) are allowed only if origin header is in whois.allow.cross.origin.hosts property
 *  Authenticated GET is allowed only if origin header is in whois.allow.cross.origin.hosts property, Access-Control-Allow-Credentials set to true
 *  UnAuthenticated GET requests are always allowed with Access-Control-Allow-Credentials set to false
 */
public class CustomCrossOriginHandler extends CrossOriginHandler {

    public CustomCrossOriginHandler(final String[] allowedHostsforCrossOrigin) {
        setAllowCredentials(true);
        setAllowedOriginPatterns(Sets.newHashSet(allowedHostsforCrossOrigin));
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {

        final String origin = request.getHeaders().get(HttpHeaders.ORIGIN);
        if(StringUtils.isEmpty(origin)) {
            return super.handle(request, response, callback);
        }

        if(request.getMethod().equalsIgnoreCase(HttpMethod.GET) && !getAllowedOriginPatterns().contains(origin)) {
            response.getHeaders().put(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            response.getHeaders().put(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "false");
        }

       return super.handle(request, response, callback);
    }
}
