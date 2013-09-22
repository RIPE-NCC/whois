package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.base.Charsets;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.io.Downloader;
import net.ripe.db.whois.common.source.SourceContext;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.GZIPInputStream;

@Component
class ApnicGrsSource extends GrsSource {
    private final String download;

    @Autowired
    ApnicGrsSource(
            @Value("${grs.import.apnic.source:}") final String source,
            final SourceContext sourceContext,
            final DateTimeProvider dateTimeProvider,
            final AuthoritativeResourceData authoritativeResourceData,
            final Downloader downloader,
            @Value("${grs.import.apnic.download:}") final String download) {
        super(source, sourceContext, dateTimeProvider, authoritativeResourceData, downloader);

        this.download = download;
    }

    @Override
    public void acquireDump(final Path path) throws IOException {
        downloader.downloadToFile(logger, new URL(download), path);
    }

    @Override
    public void handleObjects(final File file, final ObjectHandler handler) throws IOException {
        FileInputStream is = null;

        try {
            is = new FileInputStream(file);

            final BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(is), Charsets.UTF_8));
            handleLines(reader, new LineHandler() {
                @Override
                public void handleLines(final List<String> lines) {
                    handler.handle(lines);
                }
            });
        } finally {
            IOUtils.closeQuietly(is);
        }
    }
}
