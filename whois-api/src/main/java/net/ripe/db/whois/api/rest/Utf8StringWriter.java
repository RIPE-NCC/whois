package net.ripe.db.whois.api.rest;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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
@Produces({
        MediaType.TEXT_PLAIN,
        MediaType.TEXT_HTML,
        MediaType.TEXT_XML
})
public class Utf8StringWriter implements MessageBodyWriter<String> {

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == String.class
                && mediaType != null
                && "text".equalsIgnoreCase(mediaType.getType());
    }

    @Override
    public void writeTo(final String value,
                        final Class<?> type,
                        final Type genericType,
                        final Annotation[] annotations,
                        final MediaType mediaType,
                        final MultivaluedMap<String,Object> headers,
                        final OutputStream out) throws IOException {


        addCharSetWithTextMediaType(mediaType, headers);
        out.write(value.getBytes(Charset.forName(getCharset(mediaType))));
    }

    public static String getCharset(final MediaType mediaType) {
        return mediaType.getParameters()
                .getOrDefault(MediaType.CHARSET_PARAMETER, StandardCharsets.UTF_8.name());
    }

    public static void addCharSetWithTextMediaType(final MediaType mediaType, final MultivaluedMap<String, Object> headers) {
         final String charset = getCharset(mediaType);

        MediaType finalType = mediaType;

        if (!mediaType.getParameters().containsKey(MediaType.CHARSET_PARAMETER)) {
            finalType = new MediaType(
                    mediaType.getType(),
                    mediaType.getSubtype(),
                    Map.of(MediaType.CHARSET_PARAMETER, charset)
            );
        }

        headers.putSingle(HttpHeaders.CONTENT_TYPE, finalType.toString());
    }
}