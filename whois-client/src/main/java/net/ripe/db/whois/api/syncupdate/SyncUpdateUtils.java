package net.ripe.db.whois.api.syncupdate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class SyncUpdateUtils {

    private SyncUpdateUtils() {
        // do not instantiate
    }

    public static String encode(final String value) {
        return encode(value, "UTF-8");
    }

    // translate a string into application/x-www-form-urlencoded format.
    // DO NOT use to encode query parameters
    public static String encode(final String value, final String charset) {
        try {
            return URLEncoder.encode(value, charset);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
