package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.conversion.PasswordFilter;
import org.apache.commons.lang3.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;

public class HttpRequestMessage extends Message {

    public HttpRequestMessage(final HttpServletRequest request) {
        super(Messages.Type.INFO, toString(request));
    }

    private static String toString(final HttpServletRequest request) {
        return String.format("%s %s\n%s", request.getMethod(), formatUri(request), formatHttpHeaders(request));
    }

    private static String formatHttpHeaders(final HttpServletRequest request) {
        final StringBuilder builder = new StringBuilder();

        final Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            final String name = names.nextElement();
            final Enumeration<String> values = request.getHeaders(name);
            while (values.hasMoreElements()) {
                builder.append("Header: ").append(name).append('=').append(values.nextElement()).append('\n');
            }
        }

        return builder.toString();
    }

    private static String formatUri(final HttpServletRequest request) {
        if (StringUtils.isEmpty(request.getQueryString())) {
            return request.getRequestURI();
        } else {
            return String.format("%s?%s", request.getRequestURI(), PasswordFilter.filterPasswordsInUrl(request.getQueryString()));
        }
    }
}
