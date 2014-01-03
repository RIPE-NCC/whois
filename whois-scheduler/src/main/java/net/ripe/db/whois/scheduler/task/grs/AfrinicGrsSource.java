package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.domain.io.Downloader;
import net.ripe.db.whois.common.source.SourceContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.apache.bzip2.CBZip2InputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

@Component
class AfrinicGrsSource extends GrsSource {
    private final String download;

    @Autowired
    AfrinicGrsSource(
            @Value("${grs.import.afrinic.source:}") final String source,
            final SourceContext sourceContext,
            final DateTimeProvider dateTimeProvider,
            final AuthoritativeResourceData authoritativeResourceData,
            final Downloader downloader,
            @Value("${grs.import.afrinic.download:}") final String download) {
        super(source, sourceContext, dateTimeProvider, authoritativeResourceData, downloader);

        this.download = download;
    }

    @Override
    public void acquireDump(final Path path) throws IOException {
        downloader.downloadTo(logger, new URL(download), path);
    }

    @Override
    public void handleObjects(final File file, final ObjectHandler handler) throws IOException {
        FileInputStream is = null;

        try {
            is = new FileInputStream(file);

            final BufferedReader reader = new BufferedReader(new InputStreamReader(new CBZip2InputStream(is), Charsets.UTF_8));
            handleLines(reader, new LineHandler() {
                @Override
                public void handleLines(final List<String> lines) {
                    for (String line : lines) {
                        final List<String> fixedLines = Lists.newArrayList();
                        line = line.replaceAll("(?m)\\\\n", "\n");
                        line = line.replaceAll("(?m)\\\\t", "\t");

                        for (final String fixedLine : Splitter.on("\n").split(line)) {
                            if (StringUtils.isNotBlank(fixedLine)) {
                                fixedLines.add(fixedLine + "\n");
                            }
                        }

                        if (fixedLines.size() > 1) {
                            handler.handle(fixedLines);
                        }
                    }
                }
            });
        } finally {
            IOUtils.closeQuietly(is);
        }
    }
}
