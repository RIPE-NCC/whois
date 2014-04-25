package net.ripe.db.whois.api.rest.client;

import com.google.common.base.Function;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class RestClientUtils {
    private RestClientUtils() {
        // do not instantiate
    }

    public static final Function<String, String> CURLY_BRACES_ENCODING_FUNCTION = new Function<String, String>() {
        @Override
        public String apply(final String input) {
            // JerseyWebTarget treats curly braces as template variable delimiters - encode them instead so they are skipped.
            final StringBuilder builder = new StringBuilder(input.length());
            for (int i = 0; i < input.length(); i++) {
                final char c = input.charAt(i);
                switch (c) {
                    case '{':
                        builder.append("%7B");
                        break;
                    case '}':
                        builder.append("%7D");
                        break;
                    default:
                        builder.append(c);
                        break;
                }
            }
            return builder.toString();
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
