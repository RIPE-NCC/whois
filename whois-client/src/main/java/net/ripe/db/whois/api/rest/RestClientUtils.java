package net.ripe.db.whois.api.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class RestClientUtils {
    private RestClientUtils() {
    }

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
