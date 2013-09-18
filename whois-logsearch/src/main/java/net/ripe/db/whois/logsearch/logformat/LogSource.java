package net.ripe.db.whois.logsearch.logformat;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.springframework.util.FileCopyUtils;

import java.io.*;

public abstract class LogSource {

    public static String getGzippedContent(final InputStream input, final long size) throws IOException {
        int remaining = (int) size;
        final byte[] buffer = new byte[remaining];
        while (remaining > 0) {
            remaining -= input.read(buffer, buffer.length - remaining, remaining);
        }

        try (Reader reader = new InputStreamReader(new GzipCompressorInputStream(new ByteArrayInputStream(buffer)))) {
            return FileCopyUtils.copyToString(reader);
        }
    }
}
