package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.domain.io.Downloader;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.source.SourceContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

@Component
public class ArinIrrGrsSource extends GrsSource {

    private static final Set<ObjectType> OBJECT_TYPES = Sets.newHashSet(ObjectType.AS_SET, ObjectType.ROUTE, ObjectType.ROUTE6, ObjectType.ROUTE_SET);

    private final String download;

    @Autowired
    ArinIrrGrsSource(
            @Value("${grs.import.arin.source:}") final String source,
            final SourceContext sourceContext,
            final DateTimeProvider dateTimeProvider,
            final AuthoritativeResourceData authoritativeResourceData,
            final Downloader downloader,
            @Value("${grs.import.arin.irrdump:}") final String download) {
        super(source, sourceContext, dateTimeProvider, authoritativeResourceData, downloader);
        this.download = download;
    }

    @Override
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
                    final ObjectType objectType = parseObjectType(lines);
                    if (objectType != null && OBJECT_TYPES.contains(objectType)) {
                        handler.handle(lines);
                    }
                }

                @Nullable
                private ObjectType parseObjectType(final List<String> lines) {
                    if (lines.isEmpty()) {
                        return null;
                    }
                    return ObjectType.getByNameOrNull(StringUtils.substringBefore(lines.get(0), ":"));
                }
            });
        }
    }

}
