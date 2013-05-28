package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.io.Downloader;
import net.ripe.db.whois.common.source.SourceContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static net.ripe.db.whois.common.domain.CIString.ciString;

abstract class GrsSource implements InitializingBean {
    final String source;
    final SourceContext sourceContext;
    final DateTimeProvider dateTimeProvider;
    final Logger logger;
    final AuthoritativeResourceData authoritativeResourceData;
    final Downloader downloader;

    private GrsDao grsDao;

    GrsSource(final String source, final SourceContext sourceContext, final DateTimeProvider dateTimeProvider, final AuthoritativeResourceData authoritativeResourceData, final Downloader downloader) {
        this.source = source;
        this.sourceContext = sourceContext;
        this.dateTimeProvider = dateTimeProvider;
        this.logger = LoggerFactory.getLogger(String.format("%s.%s", GrsSource.class.getName(), source));
        this.authoritativeResourceData = authoritativeResourceData;
        this.downloader = downloader;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setDao(new GrsDao(logger, dateTimeProvider, source, sourceContext));
    }

    void setDao(final GrsDao grsDao) {
        this.grsDao = grsDao;
    }

    GrsDao getDao() {
        return grsDao;
    }

    abstract void acquireDump(File file) throws IOException;

    abstract void handleObjects(File file, ObjectHandler handler) throws IOException;

    public Logger getLogger() {
        return logger;
    }

    @Override
    public String toString() {
        return source;
    }

    String getSource() {
        return source;
    }

    AuthoritativeResource getAuthoritativeResource() {
        return authoritativeResourceData.getAuthoritativeResource(ciString(source));
    }

    void handleLines(final BufferedReader reader, final LineHandler lineHandler) throws IOException {
        List<String> lines = Lists.newArrayList();

        StringBuilder lineBuilder = new StringBuilder();
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            if (line.length() == 0) {
                lineBuilder = addLine(lines, lineBuilder);
                handleLines(lineHandler, lines);
                lines = Lists.newArrayList();
                continue;
            }

            final char firstChar = line.charAt(0);
            if (firstChar == '#') {
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
                logger.warn("Unexpected error handling lines starting with {}: {}", lines.isEmpty() ? "" : lines.get(0), e.getMessage());
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
