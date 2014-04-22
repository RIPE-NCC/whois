package net.ripe.db.whois.update.log;

import com.google.common.collect.Maps;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.FormatHelper;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.jdbc.driver.ResultInfo;
import net.ripe.db.whois.common.jdbc.driver.StatementInfo;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Credential;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateStatus;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class AuditLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogger.class);

    private final DateTimeProvider dateTimeProvider;
    private final OutputStream outputStream;
    private final Document doc;
    private final Element messages;
    private final Element updates;

    private Map<Update, Element> updateElements = Maps.newHashMap();
    private final Element dbupdate;

    AuditLogger(final DateTimeProvider dateTimeProvider, final OutputStream outputStream) {
        this.dateTimeProvider = dateTimeProvider;
        this.outputStream = outputStream;

        try {
            final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            doc = documentBuilder.newDocument();
            dbupdate = doc.createElement("dbupdate");
            dbupdate.setAttribute("created", FormatHelper.dateTimeToString(dateTimeProvider.getCurrentDateTime()));
            doc.appendChild(dbupdate);

            messages = doc.createElement("messages");
            dbupdate.appendChild(messages);

            updates = doc.createElement("updates");
            dbupdate.appendChild(updates);
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Creating audit logger", e);
        }
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored" /* Throwable is logged */)
    public void log(final Message message, @Nullable final Throwable t) {
        final Element typeElement = doc.createElement(message.getType().toString());

        final Element messageElement = doc.createElement("message");
        messageElement.setTextContent(message.toString());
        typeElement.appendChild(messageElement);

        if (t != null) {
            final Element throwableElement = doc.createElement("throwable");

            final Element throwableMessageElement = doc.createElement("message");
            throwableMessageElement.setTextContent(t.getMessage());
            throwableElement.appendChild(throwableMessageElement);

            final StringWriter stringWriter = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(stringWriter);

            t.printStackTrace(printWriter);
            printWriter.flush();

            final Element stacktraceElement = doc.createElement("stacktrace");
            stacktraceElement.setTextContent(stringWriter.toString());
            throwableElement.appendChild(stacktraceElement);

            typeElement.appendChild(throwableElement);
        }

        messages.appendChild(typeElement);
    }

    public void logDryRun() {
        dbupdate.appendChild(doc.createElement("dryRun"));
    }

    public void logUpdate(final Update update) {
        Element updateElement = createOrGetUpdateElement(update);

        int attempt = 1;
        try {
            attempt = Integer.parseInt(updateElement.getAttribute("attempt")) + 1;
            updates.removeChild(updateElement);
            updateElements.remove(update);

            updateElement = createOrGetUpdateElement(update);
        } catch (NumberFormatException ignored) {
        }

        updateElement.setAttribute("attempt", String.valueOf(attempt));
        updateElement.setAttribute("time", FormatHelper.dateTimeToString(dateTimeProvider.getCurrentDateTime()));

        final RpslObject updatedObject = update.getSubmittedObject();
        updateElement.appendChild(keyValue("key", updatedObject.getFormattedKey()));
        updateElement.appendChild(keyValue("operation", update.getOperation().name()));
        updateElement.appendChild(keyValue("reason", StringUtils.join(update.getDeleteReasons(), ", ")));
        updateElement.appendChild(keyValue("paragraph", update.getParagraph().getContent()));
        updateElement.appendChild(keyValue("object", updatedObject.toString()));
    }

    public void logPreparedUpdate(final PreparedUpdate preparedUpdate) {
        final Element updateElement = createOrGetUpdateElement(preparedUpdate.getUpdate());
        updateElement.appendChild(keyValue("updatedObject", preparedUpdate.getUpdatedObject()));
    }

    public void logException(final Update update, final Throwable throwable) {
        final Element updateElement = createOrGetUpdateElement(update);

        final Element exceptionElement = doc.createElement("exception");
        updateElement.appendChild(exceptionElement);

        exceptionElement.appendChild(keyValue("class", throwable.getClass().getName()));
        exceptionElement.appendChild(keyValue("message", throwable.getMessage()));

        final StringWriter stringWriter = new StringWriter();
        final PrintWriter stackTraceWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(stackTraceWriter);
        exceptionElement.appendChild(keyValue("stacktrace", stringWriter.getBuffer().toString()));
    }

    public void logDuration(final Update update, final String duration) {
        final Element updateElement = createOrGetUpdateElement(update);
        updateElement.appendChild(keyValue("duration", duration));
    }

    public void logQuery(final Update update, final StatementInfo statementInfo, final ResultInfo resultInfo) {
        final Element updateElement = createOrGetUpdateElement(update);

        final Element queryElement = doc.createElement("query");
        updateElement.appendChild(queryElement);

        queryElement.appendChild(keyValue("sql", statementInfo.getSql()));
        final Element paramsElement = doc.createElement("params");
        queryElement.appendChild(paramsElement);
        for (final Map.Entry<Integer, Object> entry : statementInfo.getParameters().entrySet()) {
            final Element param = keyValue("param", entry.getValue());
            param.setAttribute("idx", String.valueOf(entry.getKey()));
            paramsElement.appendChild(param);
        }

        final Element resultsElement = doc.createElement("results");
        queryElement.appendChild(resultsElement);

        int rowNum = 1;
        for (final List<String> row : resultInfo.getRows()) {
            final Element rowElement = doc.createElement("row");
            rowElement.setAttribute("idx", String.valueOf(rowNum++));
            resultsElement.appendChild(rowElement);

            int colNum = 0;
            for (final String column : row) {
                final Element columnElement = keyValue("column", column);
                columnElement.setAttribute("idx", String.valueOf(colNum++));
                rowElement.appendChild(columnElement);
            }
        }
    }

    public void logString(Update update, String element, String auditMessage) {
        final Element updateElement = createOrGetUpdateElement(update);
        updateElement.appendChild(keyValue(element, auditMessage));
    }

    public void logAuthenticationStrategy(Update update, String authenticationStrategy, Collection<RpslObject> maintainers) {
        final Element updateElement = createOrGetUpdateElement(update);

        final Element strategyElement = doc.createElement("AuthenticationStrategy");
        strategyElement.setAttribute("name", authenticationStrategy);
        updateElement.appendChild(strategyElement);

        for (RpslObject maintainer : maintainers) {
            strategyElement.appendChild(keyValue("candidate", maintainer.getFormattedKey()));
        }
    }

    public void logCredentials(Update update) {
        final Element updateElement = createOrGetUpdateElement(update);

        final Element credentialsElement = doc.createElement("Credentials");
        updateElement.appendChild(credentialsElement);

        for (Credential credential : update.getCredentials().all()) {
            credentialsElement.appendChild(keyValue("credential", credential.toString()));
        }
    }

    public void logAction(final Update update, final Action action) {
        final Element updateElement = createOrGetUpdateElement(update);
        updateElement.appendChild(keyValue("action", action.getDescription()));
    }

    public void logStatus(final Update update, final UpdateStatus status) {
        final Element updateElement = createOrGetUpdateElement(update);
        updateElement.appendChild(keyValue("status", status));
    }

    public void logMessage(final Update update, final Message message) {
        final Element updateElement = createOrGetUpdateElement(update);
        updateElement.appendChild(keyValue("message", message.toString()));
    }

    public void logMessage(final Update update, final RpslAttribute attribute, final Message message) {
        final Element updateElement = createOrGetUpdateElement(update);
        updateElement.appendChild(keyValue("message", MessageFormat.format("{0} (in attribute [{1}])", message.toString(), attribute.toString())));
    }

    private Element createOrGetUpdateElement(final Update update) {
        Element updateElement = updateElements.get(update);
        if (updateElement == null) {
            updateElement = doc.createElement("update");
            updates.appendChild(updateElement);
            updateElements.put(update, updateElement);
        }

        return updateElement;
    }

    private Element keyValue(final String key, final Object value) {
        final Element element = doc.createElement(key);
        element.appendChild(doc.createTextNode(String.valueOf(value)));
        return element;
    }

    public void close() {
        try {
            writeAndClose();
        } catch (IOException e) {
            LOGGER.error("IO Exception", e);
        }
    }

    private void writeAndClose() throws IOException {
        try {
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Transformer transformer = transformerFactory.newTransformer();
            final DOMSource source = new DOMSource(doc);
            final StreamResult result = new StreamResult(outputStream);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "paragraph object sql column message stacktrace value result throwable");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(source, result);
        } catch (TransformerException e) {
            LOGGER.error("Transformer exception", e);
        } finally {
            outputStream.close();
        }
    }
}
