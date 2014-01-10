package net.ripe.db.whois.update.log;

import com.google.common.base.Stopwatch;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.jdbc.driver.ResultInfo;
import net.ripe.db.whois.common.jdbc.driver.StatementInfo;
import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.update.domain.*;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;

@Component
public class LoggerContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerContext.class);

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormat.forPattern("HHmmss");
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private static final int MAXIMUM_FILENAME_LENGTH = 255;

    private final DateTimeProvider dateTimeProvider;
    private final ThreadLocal<Context> context = new ThreadLocal<>();

    @Value("${dir.update.audit.log}") private String baseDir;

    public void setBaseDir(final String baseDir) {
        this.baseDir = baseDir;
    }

    @Autowired
    public LoggerContext(final DateTimeProvider dateTimeProvider) {
        this.dateTimeProvider = dateTimeProvider;
    }

    @PostConstruct
    public void start() {
        checkDirs();
    }

    public void checkDirs() {
        getCreatedDir(baseDir);
    }

    private File getCreatedDir(final String name) {
        final File dir = new File(name);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Unable to create directory: " + dir.getAbsolutePath());
        }

        return dir;
    }

    public void init(final String folderName) {
        final LocalDateTime now = dateTimeProvider.getCurrentDateTime();
        final File dir = getCreatedDir(baseDir + FILE_SEPARATOR + DATE_FORMAT.print(now) + FILE_SEPARATOR + trim(TIME_FORMAT.print(now) + "." + sanitize(folderName), MAXIMUM_FILENAME_LENGTH));
        init(dir);
    }

    void init(final File dir) {
        if (context.get() != null) {
            throw new IllegalStateException("Context not empty");
        }

        final AtomicInteger fileNumber = new AtomicInteger();
        final AuditLogger auditLogger = new AuditLogger(dateTimeProvider, getOutputstream(getFile(dir, fileNumber.getAndIncrement(), "audit.xml")));

        context.set(new Context(dir, fileNumber, auditLogger));
        LOGGER.debug("Using dir: {}", dir.getAbsolutePath());
    }

    public void remove() {
        try {
            getContext().auditLogger.close();
        } finally {
            this.context.remove();
        }
    }

    public File getFile(final String filename) {
        final Context tempContext = getContext();
        return getFile(tempContext.baseDir, tempContext.nextFileNumber(), filename);
    }

    private File getFile(final File dir, final int prefix, final String filename) {
        final String name = StringUtils.leftPad(prefix + ".", 4, '0') + filename + ".gz";
        return new File(dir, name);
    }

    public File log(final String name, final LogCallback callback) {
        final File file = getFile(name);

        OutputStream os = null;
        try {
            os = getOutputstream(file);
            callback.log(os);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write to " + file.getAbsolutePath(), e);
        } finally {
            closeOutputStream(os);
        }

        return file;
    }

    private OutputStream getOutputstream(final File file) {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            return new GZIPOutputStream(new BufferedOutputStream(os));
        } catch (IOException e) {
            closeOutputStream(os);
            throw new IllegalStateException("Unable to write to " + file.getAbsolutePath(), e);
        }
    }

    public void log(final Message message) {
        getContext().auditLogger.log(message, null);
    }

    public void log(final Message message, final Throwable t) {
        getContext().auditLogger.log(message, t);
    }

    public void logUpdateStarted(final Update update) {
        final Context ctx = getContext();
        ctx.auditLogger.logUpdate(update);
        ctx.stopwatch = new Stopwatch().start();
        ctx.currentUpdate = update;
    }

    public void logUpdateCompleted(final UpdateContainer updateContainer) {
        logUpdateComplete(updateContainer, null);
    }

    public void logUpdateFailed(final UpdateContainer updateContainer, final Throwable throwable) {
        logUpdateComplete(updateContainer, throwable);
    }

    private void logUpdateComplete(final UpdateContainer updateContainer, final Throwable throwable) {
        final Context ctx = getContext();
        if (ctx.currentUpdate == null) {
            throw new IllegalStateException("No current update");
        }

        final AuditLogger auditLogger = ctx.auditLogger;
        final Update update = updateContainer.getUpdate();
        if (throwable != null) {
            auditLogger.logException(update, throwable);
        }

        if (ctx.stopwatch != null) {
            auditLogger.logDuration(update, ctx.stopwatch.stop().toString());
            ctx.stopwatch = null;
        }

        ctx.currentUpdate = null;
    }

    public void logPreparedUpdate(PreparedUpdate preparedUpdate) {
        getContext().auditLogger.logPreparedUpdate(preparedUpdate);
    }

    public boolean canLog() {
        final Context ctx = context.get();
        return !(ctx == null || ctx.currentUpdate == null);
    }

    public void logQuery(final StatementInfo statementInfo, final ResultInfo resultInfo) {
        final Context ctx = context.get();
        if (canLog()) {
            ctx.auditLogger.logQuery(ctx.currentUpdate, statementInfo, resultInfo);
        }
    }

    public void logDryRun() {
        getContext().auditLogger.logDryRun();
    }

    public void logAction(final UpdateContainer updateContainer, final Action action) {
        getContext().auditLogger.logAction(updateContainer.getUpdate(), action);
    }

    public void logString(final Update update, final String element, final String auditMessage) {
        getContext().auditLogger.logString(update, element, auditMessage);
    }

    public void logStatus(final UpdateContainer updateContainer, final UpdateStatus action) {
        getContext().auditLogger.logStatus(updateContainer.getUpdate(), action);
    }

    public void logMessage(final UpdateContainer updateContainer, final Message message) {
        getContext().auditLogger.logMessage(updateContainer.getUpdate(), message);
    }

    public void logMessage(final UpdateContainer updateContainer, final RpslAttribute attribute, final Message message) {
        getContext().auditLogger.logMessage(updateContainer.getUpdate(), attribute, message);
    }

    public void logException(final UpdateContainer updateContainer, final Throwable throwable) {
        getContext().auditLogger.logException(updateContainer.getUpdate(), throwable);
    }

    public void logMessages(final UpdateContainer updateContainer, ObjectMessages objectMessages) {
        for (final Message message : objectMessages.getMessages().getAllMessages()) {
            logMessage(updateContainer, message);
        }

        for (Map.Entry<RpslAttribute, Messages> entry : objectMessages.getAttributeMessages().entrySet()) {
            final RpslAttribute attribute = entry.getKey();
            for (Message message : entry.getValue().getAllMessages()) {
                logMessage(updateContainer, attribute, message);
            }
        }
    }

    private void closeOutputStream(final OutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                LOGGER.error("Closing outputstream", e);
            }
        }
    }

    private Context getContext() {
        final Context result = context.get();
        if (result == null) {
            throw new IllegalStateException("No current context");
        }

        return result;
    }

    private String sanitize(final String filename) {
        return (filename == null) ? null : filename.replaceAll("[^\\p{Alnum}\\-\\.:_]", "");
    }

    private String trim(final String filename, final int maxLength) {
        if (filename.length() <= maxLength) {
            return filename;
        }
        return filename.substring(0, maxLength - 1);
    }

    private static final class Context {
        private final File baseDir;
        private final AtomicInteger fileNumber;
        private final AuditLogger auditLogger;
        private Stopwatch stopwatch;
        private Update currentUpdate;

        private Context(final File baseDir, final AtomicInteger fileNumber, final AuditLogger auditLogger) {
            this.baseDir = baseDir;
            this.fileNumber = fileNumber;
            this.auditLogger = auditLogger;
            LOGGER.info("baseDir= {} ", baseDir.getAbsolutePath());
        }

        public int nextFileNumber() {
            return fileNumber.getAndIncrement();
        }
    }
}
