package net.ripe.db.whois.api.rest;

import com.google.common.base.Splitter;
import net.ripe.db.whois.api.rest.mapper.AttributeMapper;
import net.ripe.db.whois.api.rest.mapper.DirtyServerIncomingAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.DirtyServerOutgoingAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.FormattedServerIncomingAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.FormattedServerOutgoingAttributeMapper;
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

    public static Class<? extends AttributeMapper> getServerIncomingAttributeMapper(final boolean unformatted) {
        return unformatted ? DirtyServerIncomingAttributeMapper.class : FormattedServerIncomingAttributeMapper.class;
    }

    public static Class<? extends AttributeMapper> getServerOutgoingAttributeMapper(final boolean unformatted) {
        return unformatted ? DirtyServerOutgoingAttributeMapper.class : FormattedServerOutgoingAttributeMapper.class;
    }

    public static boolean isQueryParamSet(final String queryString, final String key) {
        if (queryString == null) {
            return false;
        }

        for (String next : AMPERSAND_SPLITTER.split(queryString)) {
            final Iterator<String> iterator = EQUALS_SPLITTER.split(next).iterator();
            if (iterator.hasNext()) {
                if (iterator.next().equals(key)) {
                    if (!iterator.hasNext()) {
                        return true;
                    }

                    if (isQueryParamSet(iterator.next())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isQueryParamSet(final String queryParam) {
        return (queryParam != null) && (queryParam.isEmpty() || queryParam.equalsIgnoreCase("true"));
    }
}
