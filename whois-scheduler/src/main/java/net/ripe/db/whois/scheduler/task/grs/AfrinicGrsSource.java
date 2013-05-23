package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.grs.AfrinicResourceData;
import net.ripe.db.whois.common.io.Downloader;
import net.ripe.db.whois.common.source.SourceContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.apache.bzip2.CBZip2InputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.util.List;

@Component
class AfrinicGrsSource extends GrsSource {
    private String download;

    @Value("${grs.import.afrinic.download:}")
    public void setDownload(final String download) {
        this.download = download;
    }

    @Autowired
    AfrinicGrsSource(
            @Value("${grs.import.afrinic.source:}") final String source,
            final SourceContext sourceContext,
            final DateTimeProvider dateTimeProvider,
            final AfrinicResourceData afrinicResourceData) {
        super(source, sourceContext, dateTimeProvider, afrinicResourceData);
    }

    @Override
    public void acquireDump(final File file) throws IOException {
        Downloader.downloadToFile(logger, new URL(download), file);
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
