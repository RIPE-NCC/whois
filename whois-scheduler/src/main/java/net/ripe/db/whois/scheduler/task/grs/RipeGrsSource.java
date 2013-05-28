package net.ripe.db.whois.scheduler.task.grs;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.io.Downloader;
import net.ripe.db.whois.common.source.SourceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class RipeGrsSource extends GrsSource {
    @Autowired
    RipeGrsSource(
            @Value("${grs.import.ripe.source:}") final String source,
            final SourceContext sourceContext,
            final DateTimeProvider dateTimeProvider,
            final AuthoritativeResourceData authoritativeResourceData,
            final Downloader downloader) {
        super(source, sourceContext, dateTimeProvider, authoritativeResourceData, downloader);
    }

    @Override
    void acquireDump(final File file) throws IOException {
        throw new UnsupportedOperationException(String.format("No dumps are available for source: %s", source));
    }

    @Override
    void handleObjects(final File file, final ObjectHandler handler) throws IOException {
        throw new UnsupportedOperationException(String.format("No import should be performed for source: %s", source));
    }
}
