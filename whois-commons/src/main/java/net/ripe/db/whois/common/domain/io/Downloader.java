package net.ripe.db.whois.common.domain.io;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.springframework.util.FileCopyUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// downloader is tested in whois-api integration tests, so that unit tests run without internet access
public interface Downloader {

    DateTimeFormatter LAST_MODIFIED_FORMAT = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss VV").withZone(ZoneId.of("GMT"));
    Pattern MD5_CAPTURE_PATTERN = Pattern.compile("([a-fA-F0-9]{32})");

    void downloadToWithMd5Check(final Logger logger, final URL url, final Path path) throws IOException;

    void downloadTo(final Logger logger, final URL url, final Path path) throws IOException;

    default void setFileTimes(final Logger logger, @Nullable final String lastModified, final Path path) {
        if (lastModified == null) {
            logger.info("Couldn't set last modified on {} because no header found", path);
        } else {
            try {
                final ZonedDateTime lastModifiedDateTime = LocalDateTime.from(LAST_MODIFIED_FORMAT.parse(lastModified)).atZone(ZoneOffset.UTC);
                final BasicFileAttributeView attributes = Files.getFileAttributeView(path, BasicFileAttributeView.class);
                final FileTime time = FileTime.from(lastModifiedDateTime.toInstant());
                attributes.setTimes(time, time, time);
                logger.debug("{} last modified {}", path, lastModifiedDateTime);
            } catch (Exception e) {
                logger.info("Couldn't set last modified {} on {} due to {}: {}", lastModified, path, e.getClass().getName(), e.getMessage());
            }
        }
    }

    default void checkMd5(final Path path, final URL url) throws IOException {
        try (InputStream resourceDataStream = Files.newInputStream(path, StandardOpenOption.READ);
             InputStream md5Stream = new URL(url + ".md5").openStream()) {
            checkMD5(resourceDataStream, md5Stream);
        }
    }

    private void checkMD5(final InputStream resourceDataStream, final InputStream md5Stream) throws IOException {
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

}
