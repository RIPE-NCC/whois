package net.ripe.db.whois.api.rest.client;

import com.google.common.base.Function;
import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class RestClientUtils {
    private RestClientUtils() {
        // do not instantiate
    }

    private static final String[] SEARCH_LIST = new String[]{"{", "}"};
    private static final String[] REPLACEMENT_LIST = new String[]{"%7B", "%7D"};

    // JerseyWebTarget treats curly braces as template variable delimiters - encode them instead so they are skipped.
    public static final Function<String, String> CURLY_BRACES_ENCODING_FUNCTION = new Function<String, String>() {
        @Override
        public String apply(final String input) {
            return StringUtils.replaceEach(input, SEARCH_LIST, REPLACEMENT_LIST);
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
