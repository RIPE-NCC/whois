package net.ripe.db.whois.common.domain.io;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import jakarta.ws.rs.core.HttpHeaders;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.domain.Timestamp;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// downloader is tested in whois-api integration tests, so that tests run without internet access
@Component
public class Downloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(Downloader.class);

    private static final Pattern MD5_CAPTURE_PATTERN = Pattern.compile("([a-fA-F0-9]{32})");
    private static final DateTimeFormatter LAST_MODIFIED_FORMAT = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss VV").withZone(ZoneId.of("GMT"));

    private static final int CONNECT_TIMEOUT = 60_000;
    private static final int READ_TIMEOUT = 60_000;

    void checkMD5(final InputStream resourceDataStream, final InputStream md5Stream) throws IOException {
        final String md5Line = FileCopyUtils.copyToString(new InputStreamReader(md5Stream, StandardCharsets.UTF_8)).trim();
        final Matcher matcher = MD5_CAPTURE_PATTERN.matcher(md5Line);
        if (!matcher.find()) {
            throw new IllegalArgumentException(String.format("Unexpected md5 hash: %s", md5Line));
        }

        final String expectedMd5 = matcher.group(1);
        final String md5 = DigestUtils.md5Hex(resourceDataStream);
        if (!md5.equalsIgnoreCase(expectedMd5)) {
            throw new IllegalArgumentException(String.format("MD5 hash invalid - expected: %s; actual: %s", expectedMd5, md5));
        }
    }

    @RetryFor(value = IOException.class, attempts = 10, intervalMs = 10000)
    public void downloadToWithMd5Check(final Logger logger, final URL url, final Path path) throws IOException {
        final URLConnection uc = url.openConnection();
        try (InputStream is = uc.getInputStream()) {
            downloadToFile(logger, is, path);
            setLastModified(uc, path);
            try (InputStream resourceDataStream = Files.newInputStream(path, StandardOpenOption.READ);
                 InputStream md5Stream = new URL(url + ".md5").openStream()) {
                checkMD5(resourceDataStream, md5Stream);
            }
        }
    }

    @RetryFor(value = IOException.class, attempts = 10, intervalMs = 10000)
    public void downloadTo(final Logger logger, final URL url, final Path path) throws IOException {
        logger.debug("Downloading {} from {}", path, url);

        final URLConnection uc = url.openConnection();
        uc.setConnectTimeout(CONNECT_TIMEOUT);
        uc.setReadTimeout(READ_TIMEOUT);

        if ("https".equals(url.getProtocol()) && !Strings.isNullOrEmpty(url.getUserInfo())) {
            uc.setRequestProperty(
                HttpHeaders.AUTHORIZATION,
                String.format("Basic %s",
                    Base64.getEncoder().encodeToString(url.getUserInfo().getBytes(StandardCharsets.UTF_8))));
        }

        try (InputStream is = uc.getInputStream()) {
            downloadToFile(logger, is, path);
            setLastModified(uc, path);
        }
    }

    void downloadToFile(final Logger logger, final InputStream is, final Path file) throws IOException {
        Files.createDirectories(file.getParent());

        final Stopwatch stopwatch = Stopwatch.createStarted();

        try (ReadableByteChannel rbc = Channels.newChannel(is);
             FileChannel fc = FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        ) {
            fc.transferFrom(rbc, 0, Long.MAX_VALUE);
        }

        if (Files.size(file) == 0) {
            throw new IllegalStateException(String.format("Empty file: %s", file));
        }

        logger.debug("Downloaded {} in {}", file, stopwatch.stop());
    }

    private boolean setLastModified(final URLConnection uc, final Path path) {
        final String lastModified = uc.getHeaderField(HttpHeaders.LAST_MODIFIED);
        boolean result = false;
        if (lastModified == null) {
            LOGGER.warn("No Last-modified header for {}", path);
        } else {
            try {
                final ZonedDateTime lastModifiedDateTime = LocalDateTime.from(LAST_MODIFIED_FORMAT.parse(lastModified)).atZone(ZoneOffset.UTC);
                result = path.toFile().setLastModified(Timestamp.from(lastModifiedDateTime.toLocalDateTime()).getValue());
                if (!result) {
                    LOGGER.warn("Unable to set last modified on {}", path);
                }
            } catch (Exception e) {
                LOGGER.warn("Couldn't parse Last-modified: {} due to {}: {}", lastModified, e.getClass().getName(), e.getMessage());
            }
        }
        return result;
    }
}
