package net.ripe.db.whois.common.io;

import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import net.ripe.db.whois.common.aspects.Retry;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Downloader {
    private static final Pattern MD5_CAPTURE_PATTERN = Pattern.compile("([a-fA-F0-9]{32})");

    static void checkMD5(final InputStream resourceDataStream, final InputStream md5Stream) throws IOException {
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

    public static void downloadGrsData(final Logger logger, final URL url, final File file) throws IOException {
        Retry.forException(new Retry.Retryable<Void>() {
            @Override
            public Void attempt() throws IOException {
                InputStream is = null;
                InputStream md5Stream = null;
                InputStream resourceDataStream = null;
                try {
                    is = url.openStream();
                    downloadToFile(logger, is, file);
                    md5Stream = new URL(String.format("%s.md5", url)).openStream();
                    resourceDataStream = new BufferedInputStream(new FileInputStream(file));
                    checkMD5(resourceDataStream, md5Stream);
                } finally {
                    IOUtils.closeQuietly(is);
                    IOUtils.closeQuietly(md5Stream);
                    IOUtils.closeQuietly(resourceDataStream);
                }
                return null;
            }
        }, 10, 10000, IOException.class);
    }

    public static void downloadToFile(final Logger logger, final URL url, final File file) throws IOException {
        logger.info("Downloading {} from {}", file, url);

        Retry.forException(new Retry.Retryable<Void>() {
            @Override
            public Void attempt() throws IOException {
                InputStream is = null;
                try {
                    is = url.openStream();
                    downloadToFile(logger, is, file);
                } finally {
                    IOUtils.closeQuietly(is);
                }
                return null;
            }
        }, 10, 10000, IOException.class);
    }

    static void downloadToFile(final Logger logger, final InputStream is, final File file) throws IOException {
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
}


