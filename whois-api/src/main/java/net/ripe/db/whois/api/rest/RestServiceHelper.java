package net.ripe.db.whois.api.rest;

import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;

public class RestServiceHelper {

    private static final String FILTER_QUERY_STRING = "([&]{0}password=[^&]*[&]?)|([&]password=[^&]*)";

    private RestServiceHelper() {
        // do not instantiate
    }

    public static String getRequestURL(final HttpServletRequest request) {
        final StringBuilder builder = new StringBuilder();
        builder.append(request.getRequestURL());
        final String queryString = filter(request.getQueryString());
        if (queryString.length() > 0) {
            builder.append('?').append(queryString);
        }
        return builder.toString();
    }

    public static String getRequestURI(final HttpServletRequest request) {
        final StringBuilder builder = new StringBuilder();
        builder.append(request.getRequestURI());
        final String queryString = filter(request.getQueryString());
        if (queryString.length() > 0) {
            builder.append('?').append(queryString);
        }
        return builder.toString();
    }

    private static String filter(final String queryString) {
        return !StringUtils.isEmpty(queryString) ? queryString.replaceAll(FILTER_QUERY_STRING, "") : "";
    }
}
