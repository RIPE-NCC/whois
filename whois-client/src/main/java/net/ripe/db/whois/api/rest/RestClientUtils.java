package net.ripe.db.whois.api.rest;

import com.google.common.base.Function;

import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class RestClientUtils {
    private RestClientUtils() {
    }

    public static final Function<String, String> ENCODING_FUNCTION = new Function<String, String>() {
        @Nullable
        @Override
        public String apply(@Nullable String input) {
            return encode(input);
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
