package net.ripe.db.whois.api.whois;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.*;
import net.ripe.db.whois.update.handler.UpdateRequestHandler;
import net.ripe.db.whois.update.log.LoggerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    @Autowired
    public InternalUpdatePerformer(final UpdateRequestHandler updateRequestHandler, final RpslObjectDao rpslObjectDao) {
        this.updateRequestHandler = updateRequestHandler;
        this.rpslObjectDao = rpslObjectDao;
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
