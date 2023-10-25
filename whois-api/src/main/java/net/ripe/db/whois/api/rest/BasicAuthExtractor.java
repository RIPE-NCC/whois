package net.ripe.db.whois.api.rest;

import com.google.common.net.HttpHeaders;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.apache.commons.codec.binary.Base64;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpStatus;

import java.nio.charset.StandardCharsets;

import static jakarta.servlet.http.HttpServletRequest.BASIC_AUTH;

public class BasicAuthExtractor {

    public static String getBasicAuth(final HttpServletRequest request){
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.toUpperCase().startsWith(BASIC_AUTH)){
            return null;
        }
        if (!HttpScheme.HTTPS.is(request.getHeader(HttpHeaders.X_FORWARDED_PROTO))){
            throw new WebApplicationException(Response.status(HttpStatus.UPGRADE_REQUIRED_426)
                    .entity(RestServiceHelper.createErrorEntity(request, RestMessages.httpVersionNotSupported()))
                    .build());
        }
        final String base64Credentials = authHeader.substring(BASIC_AUTH.length()).trim();
        final byte[] credDecoded = new Base64().decode(base64Credentials);
        return new String(credDecoded, StandardCharsets.ISO_8859_1);
    }
}
