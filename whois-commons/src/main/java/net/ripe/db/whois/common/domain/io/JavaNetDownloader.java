package net.ripe.db.whois.common.domain.io;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import jakarta.ws.rs.core.HttpHeaders;
import net.ripe.db.whois.common.aspects.RetryFor;
import org.slf4j.Logger;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;

@Primary
@Component
public class JavaNetDownloader implements Downloader {

    private static final int CONNECT_TIMEOUT = 60_000;
    private static final int READ_TIMEOUT = 60_000;

    @Override
    @RetryFor(value = IOException.class, attempts = 10, intervalMs = 10000)
    public void downloadToWithMd5Check(final Logger logger, final URL url, final Path path) throws IOException {
        final URLConnection uc = url.openConnection();
        try (InputStream is = uc.getInputStream()) {
            downloadToFile(logger, is, path);
            setFileTimes(logger, uc.getHeaderField(HttpHeaders.LAST_MODIFIED), path);
            checkMd5(path, url);
        }
    }

    @Override
    @RetryFor(value = IOException.class, attempts = 10, intervalMs = 10000)
    public void downloadTo(final Logger logger, final URL url, final Path path) throws IOException {
        logger.debug("Downloading {} from {}", path, url);

        try {
            final URLConnection uc = url.openConnection();
            uc.setConnectTimeout(CONNECT_TIMEOUT);
            uc.setReadTimeout(READ_TIMEOUT);

            if ("https".equals(url.getProtocol()) && !Strings.isNullOrEmpty(url.getUserInfo())) {
                uc.setRequestProperty(
                        HttpHeaders.AUTHORIZATION,
                        String.format("Basic %s",
                                Base64.getEncoder().encodeToString(url.getUserInfo().getBytes(StandardCharsets.UTF_8))));
            }

            try (final InputStream is = uc.getInputStream()) {
                downloadToFile(logger, is, path);
                setFileTimes(logger, uc.getHeaderField(HttpHeaders.LAST_MODIFIED), path);
            }
        } catch (final IOException ex){
            logger.error("Error downloading or setting connection for url {}", url, ex);
            throw ex;
        }
    }

    private void downloadToFile(final Logger logger, final InputStream is, final Path file) throws IOException {
        try {
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
        } catch (IOException ex){
            logger.error("Error when downloading {}", file, ex);
            throw ex;
        }
    }

}
