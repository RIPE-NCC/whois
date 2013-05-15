package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.source.SourceContext;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class GrsSource implements InitializingBean {
    private static final Pattern MD5_CAPTURE_PATTERN = Pattern.compile("([a-fA-F0-9]{32})");

    final String source;
    final String resourceDataUrl;
    final SourceContext sourceContext;
    final DateTimeProvider dateTimeProvider;
    final Logger logger;

    GrsSource(final String source, final String resourceDataUrl, final SourceContext sourceContext, final DateTimeProvider dateTimeProvider) {
        this.source = source;
        this.resourceDataUrl = resourceDataUrl;
        this.sourceContext = sourceContext;
        this.dateTimeProvider = dateTimeProvider;
        this.logger = LoggerFactory.getLogger(String.format("%s.%s", GrsSource.class.getName(), source));
    }

    private GrsDao grsDao;

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

    void acquireResourceData(final File file) throws IOException {
        if (StringUtils.isBlank(resourceDataUrl)) {
            logger.warn("No resource data for {}", source);
            return;
        }

        downloadToFile(new URL(resourceDataUrl), file);

        InputStream resourceDataStream = null;
        InputStream md5Stream = null;

        try {
            resourceDataStream = new BufferedInputStream(new FileInputStream(file));
            md5Stream = new URL(String.format("%s.md5", resourceDataUrl)).openStream();
            checkMD5(resourceDataStream, md5Stream);
        } finally {
            IOUtils.closeQuietly(resourceDataStream);
            IOUtils.closeQuietly(md5Stream);
        }
    }

    void checkMD5(final InputStream resourceDataStream, final InputStream md5Stream) throws IOException {
        final String md5Line = FileCopyUtils.copyToString(new InputStreamReader(md5Stream, Charsets.UTF_8));
        final Matcher matcher = MD5_CAPTURE_PATTERN.matcher(md5Line);
        if (!matcher.find()) {
            throw new IllegalArgumentException(String.format("Unexpected md5 line: %s", md5Line));
        }

        final String expectedMd5 = matcher.group(1);
        final String md5 = DigestUtils.md5Hex(resourceDataStream);
        if (!md5.equalsIgnoreCase(expectedMd5)) {
            throw new IllegalArgumentException(String.format("MD5 error - expected: %s; actual: %s", expectedMd5, md5));
        }
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

    void downloadToFile(final URL url, final File file) throws IOException {
        logger.info("Downloading {} from {}", file, url);
        InputStream is = null;

        try {
            is = url.openStream();
            downloadToFile(is, file);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    void downloadToFile(final InputStream is, final File file) throws IOException {
        if (file.mkdirs()) {
            logger.info("Created dirs for {}", file);
        }

        if (file.exists()) {
            if (file.delete()) {
                logger.debug("Delete existing {}", file);
            } else {
                throw new IllegalStateException("Unable to delete " + file);
            }
        }

        final Stopwatch stopwatch = new Stopwatch().start();

        ReadableByteChannel rbc = null;
        FileOutputStream fos = null;

        try {
            rbc = Channels.newChannel(is);
            fos = new FileOutputStream(file);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } finally {
            IOUtils.closeQuietly(rbc);
            IOUtils.closeQuietly(fos);
        }

        if (file.length() == 0) {
            throw new IllegalStateException(String.format("Empty file: %s", file));
        }

        logger.info("Downloaded {} in {}", file, stopwatch.stop());
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
