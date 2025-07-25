package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.io.Downloader;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.source.SourceContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static net.ripe.db.whois.common.domain.CIString.ciString;

abstract class GrsSource implements InitializingBean {
    final CIString name;
    final SourceContext sourceContext;
    final DateTimeProvider dateTimeProvider;
    final Logger logger;
    final AuthoritativeResourceData authoritativeResourceData;
    final Downloader downloader;

    private GrsDao grsDao;

    GrsSource(final String name, final SourceContext sourceContext, final DateTimeProvider dateTimeProvider, final AuthoritativeResourceData authoritativeResourceData, final Downloader downloader) {
        this.name = ciString(name);
        this.sourceContext = sourceContext;
        this.dateTimeProvider = dateTimeProvider;
        this.logger = LoggerFactory.getLogger(String.format("%s.%s", GrsSource.class.getName(), name));
        this.authoritativeResourceData = authoritativeResourceData;
        this.downloader = downloader;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setDao(new GrsDao(logger, dateTimeProvider, name, sourceContext));
    }

    void setDao(final GrsDao grsDao) {
        this.grsDao = grsDao;
    }

    GrsDao getDao() {
        return grsDao;
    }

    abstract void acquireDump(Path path) throws IOException;

    public void acquireIrrDump(Path path) throws IOException {};

    abstract void handleObjects(File file, ObjectHandler handler) throws IOException;

    public void handleIrrObjects(File file, ObjectHandler handler) throws IOException {};

    public Logger getLogger() {
        return logger;
    }

    @Override
    public String toString() {
        return name.toString();
    }

    CIString getName() {
        return name;
    }

    AuthoritativeResource getAuthoritativeResource() {
        return authoritativeResourceData.getAuthoritativeResource(name);
    }

    void handleLines(final BufferedReader reader, final LineHandler lineHandler) throws IOException {
        List<String> lines = Lists.newArrayList();

        StringBuilder lineBuilder = new StringBuilder();
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            if (line.isEmpty()) {
                lineBuilder = addLine(lines, lineBuilder);
                handleLines(lineHandler, lines);
                lines = Lists.newArrayList();
                continue;
            }

            final char firstChar = line.charAt(0);
            if (firstChar == '#' || firstChar == '%') {
                continue;
            }

            if (firstChar != ' ' && firstChar != '+') {
                lineBuilder = addLine(lines, lineBuilder);
            }

            lineBuilder.append(line).append('\n');
        }

        addLine(lines, lineBuilder);
        handleLines(lineHandler, lines);
    }

    private void handleLines(final LineHandler lineHandler, final List<String> lines) {
        if (!lines.isEmpty()) {
            try {
                lineHandler.handleLines(lines);
            } catch (RuntimeException e) {
                logger.info("Unexpected {} handling lines starting with {}: {}", e.getClass().getName(), lines.isEmpty() ? "" : lines.getFirst(), e.getMessage());
            }
        }
    }

    private StringBuilder addLine(final List<String> lines, final StringBuilder lineBuilder) {
        final String line = lineBuilder.toString();
        if (StringUtils.isNotBlank(line)) {
            lines.add(line);
        }

        return new StringBuilder();
    }

    interface LineHandler {
        void handleLines(List<String> lines);
    }
}
