package net.ripe.db.whois.scheduler.task.grs;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.domain.io.Downloader;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.source.SourceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.GZIPInputStream;

@Component
class JpirrGrsSource extends GrsSource {
    private final String download;

    @Autowired
    JpirrGrsSource(
            @Value("${grs.import.jpirr.source:}") final String source,
            final SourceContext sourceContext,
            final DateTimeProvider dateTimeProvider,
            final AuthoritativeResourceData authoritativeResourceData,
            final Downloader downloader,
            @Value("${grs.import.jpirr.download:}") final String download) {
        super(source, sourceContext, dateTimeProvider, authoritativeResourceData, downloader);

        this.download = download;
    }

    @Override
    @RetryFor(value = IOException.class, attempts = 5, intervalMs = 60000)
    public void acquireDump(final Path path) throws IOException {
        downloader.downloadTo(logger, new URL(download), path);
    }

    @Override
    public void handleObjects(final File file, final ObjectHandler handler) throws IOException {
        try (FileInputStream is = new FileInputStream(file)) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(is), StandardCharsets.UTF_8));
            handleLines(reader, new LineHandler() {
                @Override
                public void handleLines(final List<String> lines) {
                    if (!lines.isEmpty() && !lines.get(0).startsWith("*xx")) {
                        handler.handle(lines);
                    }
                }
            });
        }
    }
}
