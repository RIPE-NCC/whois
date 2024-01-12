package net.ripe.db.whois.api.syncupdate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class SyncUpdateUtils {

    private SyncUpdateUtils() {
        // do not instantiate
    }

    public static String encode(final String value) {
        return encode(value, StandardCharsets.UTF_8);
    }

    // translate a string into application/x-www-form-urlencoded format.
    // DO NOT use to encode query parameters
    public static String encode(final String value, final Charset charset) {
        try {
            return URLEncoder.encode(value, charset.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
