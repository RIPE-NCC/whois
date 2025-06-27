package net.ripe.db.whois.api.httpserver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.common.x509.X509CertificateWrapper;
import org.springframework.stereotype.Component;

import java.security.cert.X509Certificate;
/**
 * Return TLS client certificate information to the client.
 */
@Component
@Path("/client")
public class ClientCertificateService {

    @GET
    public Response clientCertificate(@Context final HttpServletRequest request) {
        final Object certAttr = request.getAttribute("jakarta.servlet.request.X509Certificate");
        if (!(certAttr instanceof X509Certificate[] certificates)) {
            throw new BadRequestException("Didn't find any client certificate");
        }

        final StringBuilder builder = new StringBuilder();
        builder.append("Found ").append(certificates.length).append(" certificate(s).\n");
        for (X509Certificate certificate : certificates) {
            try {
                builder.append(new X509CertificateWrapper(certificate).getCertificateAsString()).append("\n");
            } catch (IllegalArgumentException e) {
                builder.append(e.getMessage()).append("\n");
            }
        }
        return Response.ok().entity(builder.toString()).build();
    }
}