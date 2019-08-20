package net.ripe.db.whois.api.rest;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.AttributeMapper;
import net.ripe.db.whois.api.rest.mapper.DirtyServerAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.DirtySuppressChangedAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.FormattedServerAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.RegularSuppressChangedAttributeMapper;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.sso.CrowdClientException;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.PreparedStatementCallback;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class RestServiceHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestServiceHelper.class);

    private static final Splitter AMPERSAND_SPLITTER = Splitter.on('&').omitEmptyStrings();
    private static final Splitter EQUALS_SPLITTER = Splitter.on('=').omitEmptyStrings();
    private static final int STATUS_TOO_MANY_REQUESTS = 429;

    private static final Set<Class> SKIP_STACK_TRACE = Sets.newHashSet(
                                                        CrowdClientException.class,
                                                        CannotGetJdbcConnectionException.class,
                                                        PreparedStatementCallback.class);

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

    public static Class<? extends AttributeMapper> getServerAttributeMapper(final boolean unformatted) {
        return unformatted ? DirtyServerAttributeMapper.class : FormattedServerAttributeMapper.class;
    }

    public static Class<? extends AttributeMapper> getRestResponseAttributeMapper(String queryString){
        return isQueryParamSet(queryString, "unformatted") ?
                DirtySuppressChangedAttributeMapper.class : RegularSuppressChangedAttributeMapper.class;
    }

    public static WhoisResources createErrorEntity(final HttpServletRequest request, final Message... errorMessage) {
        return createErrorEntity(request, Arrays.asList(errorMessage));
    }

    public static WhoisResources createErrorEntity(final HttpServletRequest request, final List<Message> errorMessages) {
        final WhoisResources whoisResources = new WhoisResources();
        whoisResources.setErrorMessages(createErrorMessages(errorMessages));
        // TODO: [AH] the external URL should be configurable via properties
        whoisResources.setLink(Link.create(getRequestURL(request).replaceFirst("/whois", "")));
        whoisResources.includeTermsAndConditions();
        return whoisResources;
    }

    public static List<ErrorMessage> createErrorMessages(final List<Message> messages) {
        final List<ErrorMessage> errorMessages = Lists.newArrayList();

        for (Message message : messages) {
            errorMessages.add(new ErrorMessage(message));
        }

        return errorMessages;
    }

    public static WebApplicationException createWebApplicationException(final RuntimeException exception, final HttpServletRequest request) {
        return createWebApplicationException(exception, request, Lists.newArrayList());
    }

    public static WebApplicationException createWebApplicationException(final RuntimeException exception, final HttpServletRequest request, final List<Message> messages) {
        final Response.ResponseBuilder responseBuilder;

        if (exception instanceof QueryException) {
            final QueryException queryException = (QueryException) exception;
            if (queryException.getCompletionInfo() == QueryCompletionInfo.BLOCKED) {
                responseBuilder = Response.status(STATUS_TOO_MANY_REQUESTS);
            } else {
                responseBuilder = Response.status(Response.Status.BAD_REQUEST);
            }
            messages.addAll(queryException.getMessages());

        } else {
            if (skipStackTrace(exception)) {
                LOGGER.error("{}: {}", exception.getClass().getName(), exception.getMessage());
            } else {
                LOGGER.error(exception.getMessage(), exception);
            }

            responseBuilder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);

            messages.add(QueryMessages.internalErroroccurred());
        }

        if (! messages.isEmpty()) {
            responseBuilder.entity(createErrorEntity(request, messages));
        }

        return new WebApplicationException(responseBuilder.build());
    }

    private static boolean skipStackTrace(final Exception exception) {
        return SKIP_STACK_TRACE.contains(exception.getClass());
    }
}
