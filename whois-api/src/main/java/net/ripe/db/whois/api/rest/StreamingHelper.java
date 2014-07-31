package net.ripe.db.whois.api.rest;

import com.google.common.base.Splitter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.OutputStream;

public class StreamingHelper {

    public static StreamingMarshal getStreamingMarshal(final HttpServletRequest request, final OutputStream outputStream) {
        final String acceptHeader = request.getHeader(HttpHeaders.ACCEPT);
        if (acceptHeader != null) {
            for (final String accept : Splitter.on(',').split(acceptHeader)) {
                try {
                    final MediaType mediaType = MediaType.valueOf(accept);
                    final String subtype = mediaType.getSubtype().toLowerCase();
                    if (subtype.equals("json") || subtype.endsWith("+json")) {
                        return new StreamingMarshalJson(outputStream);
                    } else if (subtype.equals("xml") || subtype.endsWith("+xml")) {
                        return new StreamingMarshalXml(outputStream, "whois-resources");
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        return new StreamingMarshalXml(outputStream, "whois-resources");
    }
}
