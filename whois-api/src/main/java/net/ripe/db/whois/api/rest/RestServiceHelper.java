package net.ripe.db.whois.api.rest;

import com.google.common.base.Splitter;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;

public class RestServiceHelper {
    private static final Splitter AMPERSAND_SPLITTER = Splitter.on('&').omitEmptyStrings();
    private static final Splitter EQUALS_SPLITTER = Splitter.on('=').omitEmptyStrings();

    private RestServiceHelper() {
        // do not instantiate
    }

    public static String getRequestURL(final HttpServletRequest request) {
            return request.getRequestURL().toString() + filter(request.getQueryString());
    }

    public static String getRequestURI(final HttpServletRequest request) {
            return request.getRequestURI() + filter(request.getQueryString());
    }

    private static String filter(final String queryString) {
        if (StringUtils.isEmpty(queryString)) {
            return "";
        }

        final StringBuilder builder = new StringBuilder();
        char separator = '?';

        for (String next : AMPERSAND_SPLITTER.split(queryString)) {
            final Iterator<String> iterator = EQUALS_SPLITTER.split(next).iterator();
            if (iterator.hasNext() && iterator.next().equalsIgnoreCase("password")) {
                continue;
            }

            builder.append(separator).append(next);
            separator = '&';
        }

        return builder.toString();
    }
}
