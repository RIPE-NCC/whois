package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Credential;
import net.ripe.db.whois.update.domain.Credentials;
import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Origin;
import net.ripe.db.whois.update.domain.OverrideCredential;
import net.ripe.db.whois.update.domain.Paragraph;
import net.ripe.db.whois.update.domain.PasswordCredential;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateRequest;
import net.ripe.db.whois.update.domain.UpdateResponse;
import net.ripe.db.whois.update.domain.UpdateStatus;
import net.ripe.db.whois.update.handler.UpdateRequestHandler;
import net.ripe.db.whois.update.log.LoggerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class InternalUpdatePerformer {
    private static final Pattern UPDATE_RESPONSE_ERRORS = Pattern.compile("(?m)^\\*\\*\\*Error:\\s*((.*)(\\n[ ]+.*)*)$");

    private final UpdateRequestHandler updateRequestHandler;
    private final RpslObjectDao rpslObjectDao;
    private final DateTimeProvider dateTimeProvider;

    @Autowired
    public InternalUpdatePerformer(final UpdateRequestHandler updateRequestHandler, final RpslObjectDao rpslObjectDao, final DateTimeProvider dateTimeProvider) {
        this.updateRequestHandler = updateRequestHandler;
        this.rpslObjectDao = rpslObjectDao;
        this.dateTimeProvider = dateTimeProvider;
    }

    public RpslObject performUpdate(final Origin origin, final Update update, final String content, final Keyword keyword, final LoggerContext loggerContext) {
        loggerContext.init(getRequestId(origin.getFrom()));
        try {
            final UpdateContext updateContext = new UpdateContext(loggerContext);
            final boolean notificationsEnabled = true;

            final UpdateRequest updateRequest = new UpdateRequest(
                    origin,
                    keyword,
                    content,
                    Lists.newArrayList(update),
                    notificationsEnabled);

            final UpdateResponse response = updateRequestHandler.handle(updateRequest, updateContext);

            if (updateContext.getStatus(update) == UpdateStatus.FAILED_AUTHENTICATION) {
                throw new WebApplicationException(getResponse(new UpdateResponse(UpdateStatus.FAILED_AUTHENTICATION, response.getResponse())));
            }
            if (response.getStatus() != UpdateStatus.SUCCESS) {
                throw new WebApplicationException(getResponse(response));
            }

            return updateContext.getPreparedUpdate(update).getUpdatedObject();
        } finally {
            loggerContext.remove();
        }
    }

    public Update createUpdate(final RpslObject rpslObject, final List<String> passwords, final String deleteReason, String override) {
        return new Update(
                createParagraph(rpslObject, passwords, override),
                deleteReason != null ? Operation.DELETE : Operation.UNSPECIFIED,
                deleteReason != null ? Lists.newArrayList(deleteReason) : null,
                rpslObject);
    }

    private Paragraph createParagraph(final RpslObject rpslObject, final List<String> passwords, String override) {
        final Set<Credential> credentials = Sets.newHashSet();
        for (String password : passwords) {
            credentials.add(new PasswordCredential(password));
        }
        if (override != null) {
            credentials.add(OverrideCredential.parse(override));
        }

        return new Paragraph(rpslObject.toString(), new Credentials(credentials));
    }

    public Origin createOrigin(final HttpServletRequest request) {
        return new WhoisRestApi(dateTimeProvider, request.getRemoteAddr());
    }

    public String createContent(final RpslObject rpslObject, final List<String> passwords, final String deleteReason, String override) {
        final StringBuilder builder = new StringBuilder();
        builder.append(rpslObject.toString());

        if (builder.charAt(builder.length() - 1) != '\n') {
            builder.append('\n');
        }

        if (deleteReason != null) {
            builder.append("delete: ");
            builder.append(deleteReason);
            builder.append("\n\n");
        }

        if (passwords != null) {
            for (final String password : passwords) {
                builder.append("password: ");
                builder.append(password);
                builder.append('\n');
            }
        }

        if (override != null) {
            builder.append("override: ");
            builder.append(override);
            builder.append("\n\n");
        }

        return builder.toString();
    }

    private Response getResponse(final UpdateResponse updateResponse) {
        int status;
        switch (updateResponse.getStatus()) {
            case FAILED: {
                status = HttpServletResponse.SC_BAD_REQUEST;

                final String errors = findAllErrors(updateResponse);
                if (!errors.isEmpty()) {
                    if (errors.contains("Enforced new keyword specified, but the object already exists")) {
                        status = HttpServletResponse.SC_CONFLICT;
                    }
                    return Response.status(status).entity(errors).build();
                }

                break;
            }
            case EXCEPTION:
                status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                break;
            case FAILED_AUTHENTICATION:
                status = HttpServletResponse.SC_UNAUTHORIZED;
                final String errors = findAllErrors(updateResponse);
                if (!errors.isEmpty()) {
                    return Response.status(status).entity(errors).build();
                }
                break;
            default:
                status = HttpServletResponse.SC_OK;
        }

        return Response.status(status).build();
    }

    private String getRequestId(final String remoteAddress) {
        return String.format("rest_%s_%s", remoteAddress, System.nanoTime());
    }

    private String findAllErrors(final UpdateResponse updateResponse) {
        final StringBuilder builder = new StringBuilder();
        final Matcher matcher = UPDATE_RESPONSE_ERRORS.matcher(updateResponse.getResponse());
        while (matcher.find()) {
            builder.append(matcher.group(1).replaceAll("[\\n ]+", " "));
            builder.append('\n');
        }
        return builder.toString();
    }
}
