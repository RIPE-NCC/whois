package net.ripe.db.whois.update.handler.response;

import com.google.common.base.Splitter;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.FormatHelper;
import net.ripe.db.whois.common.domain.Hosts;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Ack;
import net.ripe.db.whois.update.domain.Notification;
import net.ripe.db.whois.update.domain.Origin;
import net.ripe.db.whois.update.domain.ResponseMessage;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.NullLogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.StringWriter;

@Component
public class ResponseFactory {
    private static final Splitter LINE_SPLITTER = Splitter.on('\n');

    private static final String TEMPLATE_EXCEPTION = "templates/exception.vm";
    private static final String TEMPLATE_ACK = "templates/ack.vm";
    private static final String TEMPLATE_HELP = "templates/help.vm";
    private static final String TEMPLATE_NOTIFICATION = "templates/notification.vm";
    private static final String TEMPLATE_PENDING_UPDATE_TIMEOUT = "templates/pendingUpdateTimeout.vm";

    private final VelocityEngine velocityEngine;
    private final DateTimeProvider dateTimeProvider;

    private String version;

    @Value("${application.version}")
    public void setVersion(final String version) {
        this.version = version;
    }

    private String source;

    @Value("${whois.source}")
    public void setSource(final String source) {
        this.source = source;
    }

    @Autowired
    public ResponseFactory(final DateTimeProvider dateTimeProvider) {
        this.dateTimeProvider = dateTimeProvider;

        velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, new NullLogChute());
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();
    }

    public String createExceptionResponse(final UpdateContext updateContext, final Origin origin) {
        final VelocityContext velocityContext = new VelocityContext();

        return createResponse(TEMPLATE_EXCEPTION, updateContext, velocityContext, origin);
    }

    public String createAckResponse(final UpdateContext updateContext, final Origin origin, final Ack ack) {
        final VelocityContext velocityContext = new VelocityContext();

        velocityContext.put("ack", ack);
        velocityContext.put("updateContext", updateContext);

        return createResponse(TEMPLATE_ACK, updateContext, velocityContext, origin);
    }

    public String createHelpResponse(final UpdateContext updateContext, final Origin origin) {
        final VelocityContext velocityContext = new VelocityContext();
        return createResponse(TEMPLATE_HELP, updateContext, velocityContext, origin);
    }

    public ResponseMessage createNotification(final UpdateContext updateContext, final Origin origin, final Notification notification) {
        final VelocityContext velocityContext = new VelocityContext();

        velocityContext.put("failedAuthentication", notification.getUpdates(Notification.Type.FAILED_AUTHENTICATION));
        velocityContext.put("success", notification.getUpdates(Notification.Type.SUCCESS));
        velocityContext.put("successReference", notification.getUpdates(Notification.Type.SUCCESS_REFERENCE));
        velocityContext.put("pendingUpdate", notification.getUpdates(Notification.Type.PENDING_UPDATE));

        final String subject;
        if (notification.has(Notification.Type.PENDING_UPDATE)) {
            subject = "RIPE Database updates, auth request notification";
        } else if (notification.has(Notification.Type.FAILED_AUTHENTICATION)) {
            subject = "RIPE Database updates, auth error notification";
        } else {
            subject = "Notification of RIPE Database changes";
        }

        return new ResponseMessage(subject, createResponse(TEMPLATE_NOTIFICATION, updateContext, velocityContext, origin));
    }

    public ResponseMessage createPendingUpdateTimeout(final UpdateContext updateContext, final Origin origin, final RpslObject rpslObject, final int days) {
        final VelocityContext velocityContext = new VelocityContext();

        velocityContext.put("object", rpslObject);
        velocityContext.put("timeout", days);

        final String subject = String.format("Notification of RIPE Database pending update timeout on %s", rpslObject.getFormattedKey());

        return new ResponseMessage(subject, createResponse(TEMPLATE_PENDING_UPDATE_TIMEOUT, updateContext, velocityContext, origin));
    }

    private String createResponse(final String templateName, final UpdateContext updateContext, final VelocityContext velocityContext, final Origin origin) {
        velocityContext.put("globalMessages", updateContext.printGlobalMessages());
        velocityContext.put("origin", origin);
        velocityContext.put("version", version);
        velocityContext.put("hostName", Hosts.getLocalHost().name());
        velocityContext.put("source", source);
        velocityContext.put("timestamp", FormatHelper.dateTimeToString(dateTimeProvider.getCurrentDateTime()));

        final Template template = velocityEngine.getTemplate(templateName);
        final StringWriter writer = new StringWriter();

        template.merge(velocityContext, writer);

        return cleanupResponse(writer.toString());
    }

    private String cleanupResponse(final String string) {
        final StringBuilder result = new StringBuilder(string.length());

        boolean literal = false;
        boolean newline = false;
        for (String line : LINE_SPLITTER.split(string)) {
            if (line.equals(">>>>>")) {
                literal = true;
                if (newline) {
                    result.append('\n');
                    newline = false;
                }
                continue;
            } else if (line.equals("<<<<<")) {
                literal = false;
                continue;
            }

            if (literal) {
                result.append(line).append('\n');
                continue;
            }

            line = line.trim();
            if (line.equals("")) {
                newline = true;
                continue;
            }

            if (newline && result.length() != 0) {
                result.append('\n');
            }

            newline = false;
            result.append(line).append('\n');
        }

        return result.toString();
    }
}
