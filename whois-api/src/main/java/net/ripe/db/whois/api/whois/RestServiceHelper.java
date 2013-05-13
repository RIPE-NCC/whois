package net.ripe.db.whois.api.whois;

import javax.servlet.http.HttpServletRequest;

public class RestServiceHelper {

    public static String getRequestURL(final HttpServletRequest request) {
        final StringBuilder builder = new StringBuilder();
        builder.append(request.getRequestURL());
        final String queryString = request.getQueryString();
        if (queryString != null) {
            builder.append('?');
            builder.append(queryString);
        }
        return builder.toString();
    }

}
