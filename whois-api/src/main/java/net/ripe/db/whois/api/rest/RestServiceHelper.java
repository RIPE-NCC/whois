package net.ripe.db.whois.api.rest;

import com.google.common.base.Splitter;
import net.ripe.db.whois.api.rest.mapper.AttributeMapper;
import net.ripe.db.whois.api.rest.mapper.DirtyServerAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.FormattedServerAttributeMapper;
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
        final String queryString = request.getQueryString();
        final StringBuffer requestURL = request.getRequestURL();

        if (StringUtils.isBlank(queryString)) {
            return requestURL.toString();
        }

        final StringBuilder builder = new StringBuilder(requestURL);
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

    public static boolean isQueryParamSet(final String queryString, final String key) {
        if (queryString == null) {
            return false;

        }

        for (String next : AMPERSAND_SPLITTER.split(queryString)) {
            final Iterator<String> iterator = EQUALS_SPLITTER.split(next).iterator();
            if (iterator.hasNext()) {
                // check if query parameter is present, and has no value, or value is true
                if (iterator.next().equals(key) &&
                        (!iterator.hasNext() || iterator.next().equals("true"))) {
                    return true;
                }
            }
        }

        return false;
    }

    public static Class<? extends AttributeMapper> getServerAttributeMapper(HttpServletRequest request){
        return isQueryParamSet(request.getQueryString(), "dirty") ?
                DirtyServerAttributeMapper.class : FormattedServerAttributeMapper.class;
    }

}
