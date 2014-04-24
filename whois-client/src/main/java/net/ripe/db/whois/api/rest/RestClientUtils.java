package net.ripe.db.whois.api.rest;

import com.google.common.base.Function;
import org.glassfish.jersey.uri.UriComponent;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class RestClientUtils {
    private RestClientUtils() {
    }

    public static final Function<String, String> ENCODING_FUNCTION = new Function<String, String>() {
        @Override
        public String apply(final String input) {
            return UriComponent.encode(input, UriComponent.Type.QUERY_PARAM, false);
        }
    };

    public static final String encode(final String param) {
        return encode(param, "UTF-8");
    }

    public static final String encode(final String param, final String encoding) {
        try {
            return URLEncoder.encode(param, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
