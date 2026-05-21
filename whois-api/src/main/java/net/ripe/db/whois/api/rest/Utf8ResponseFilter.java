package net.ripe.db.whois.api.rest;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Ensures that all {@code text/*} responses are written using UTF-8.
 *
 * <p>Starting with Jakarta Servlet 6.0 (EE11), the Servlet specification
 * mandates that if a response has a {@code Content-Type} starting with
 * {@code text/} and no charset parameter has been explicitly set,
 * the container must default the charset to ISO-8859-1.
 *
 * <p>In Jetty 12 (EE11), this behavior is strictly enforced. As a result,
 * responses such as {@code text/plain} or {@code text/html} without an
 * explicit charset will be sent as:
 *
 * <pre>
 *   Content-Type: text/plain;charset=ISO-8859-1
 * </pre>
 *
 * <p>This provider ensures that all {@code text/*} media types
 * include {@code charset=UTF-8} unless explicitly specified otherwise.
 *
 * <p>Non-text media types (e.g. {@code application/json},
 * {@code application/rdap+json}) are unaffected and are intentionally not
 * modified by this writer.
 *
 * <p>This class exists to preserve UTF-8 behavior after upgrading to EE11.
 */
@Provider
public class Utf8ResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(final ContainerRequestContext requestContext, final  ContainerResponseContext responseContext) throws IOException {

        final MediaType mediaType = responseContext.getMediaType();
        if (mediaType == null) {
            return;
        }

        //By default, is utf8
        if(mediaType.isCompatible(MediaType.TEXT_PLAIN_TYPE)) {
            final String addUtf8 = mediaType.withCharset(StandardCharsets.UTF_8.name()).toString();
            responseContext.getHeaders().putSingle( HttpHeaders.CONTENT_TYPE,  addUtf8);
        }
    }
}