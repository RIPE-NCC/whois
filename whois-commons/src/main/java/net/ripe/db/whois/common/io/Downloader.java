package net.ripe.db.whois.common.io;

import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import net.ripe.db.whois.common.aspects.RetryFor;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// downloader is tested in whois-api integration tests, so that tests run without internet access
@Component
public class Downloader {
    private static final Pattern MD5_CAPTURE_PATTERN = Pattern.compile("([a-fA-F0-9]{32})");

    void checkMD5(final InputStream resourceDataStream, final InputStream md5Stream) throws IOException {
        final String md5Line = FileCopyUtils.copyToString(new InputStreamReader(md5Stream, Charsets.UTF_8)).trim();
        final Matcher matcher = MD5_CAPTURE_PATTERN.matcher(md5Line);
        if (!matcher.find()) {
            throw new IllegalArgumentException(String.format("Unexpected md5 hash: %s", md5Line));
        }

        final String expectedMd5 = matcher.group(1);
        final String md5 = DigestUtils.md5Hex(resourceDataStream);
        if (!md5.equalsIgnoreCase(expectedMd5)) {
            throw new IllegalArgumentException(String.format("MD5 has invalid - expected: %s; actual: %s", expectedMd5, md5));
        }
    }

    @RetryFor(value = IOException.class, attempts = 10, intervalMs = 10000)
    public void downloadToWithMd5Check(final Logger logger, final URL url, final Path path) throws IOException {
        try (InputStream is = url.openStream()) {
            downloadToFile(logger, is, path);

            try (InputStream resourceDataStream = Files.newInputStream(path, StandardOpenOption.READ);
                 InputStream md5Stream = new URL(url + ".md5").openStream()) {
                checkMD5(resourceDataStream, md5Stream);
            }
        }
    }

    @RetryFor(value = IOException.class, attempts = 10, intervalMs = 10000)
    public void downloadTo(final Logger logger, final URL url, final Path path) throws IOException {
        logger.debug("Downloading {} from {}", path, url);

        try (InputStream is = url.openStream()) {
            downloadToFile(logger, is, path);
        }
    }

    void downloadToFile(final Logger logger, final InputStream is, final Path file) throws IOException {
        Files.createDirectories(file.getParent());

        final Stopwatch stopwatch = new Stopwatch().start();

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
}
