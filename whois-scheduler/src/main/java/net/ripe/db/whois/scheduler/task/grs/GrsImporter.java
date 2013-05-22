package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.scheduler.DailyScheduledTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Component
public class GrsImporter implements DailyScheduledTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrsImporter.class);
    private static final Splitter SOURCES_SPLITTER = Splitter.on(",").trimResults().omitEmptyStrings();

    private final GrsSourceImporter grsSourceImporter;
    private final Map<CIString, GrsSource> grsSources;
    private final AtomicInteger threadNum = new AtomicInteger();
    private final Set<CIString> currentlyImporting = Collections.synchronizedSet(Sets.<CIString>newHashSet());
    private final ThreadGroup threadGroup = new ThreadGroup("grs-import");

    private boolean grsImportEnabled;

    @Value("${grs.import.enabled}")
    public void setGrsImportEnabled(final boolean grsImportEnabled) {
        LOGGER.info("GRS import enabled: {}", grsImportEnabled);
        this.grsImportEnabled = grsImportEnabled;
    }

    private String defaultSources;

    @Value("${grs.import.sources}")
    public void setDefaultSources(final String defaultSources) {
        this.defaultSources = defaultSources;
    }

    @Autowired
    public GrsImporter(final GrsSourceImporter grsSourceImporter, final GrsSource[] grsSources) {
        this.grsSourceImporter = grsSourceImporter;
        this.grsSources = Maps.newHashMapWithExpectedSize(grsSources.length);
        for (final GrsSource grsSource : grsSources) {
            this.grsSources.put(ciString(grsSource.getSource()), grsSource);
        }
    }

    @Override
    public void run() {
        if (!grsImportEnabled) {
            LOGGER.info("GRS import is not enabled");
            return;
        }

        grsImport(defaultSources, false);
    }

    public void grsImport(String sources, final boolean rebuild) {
        final Set<CIString> sourcesToImport = Sets.newLinkedHashSet();
        for (final String source : SOURCES_SPLITTER.split(sources)) {
            sourcesToImport.add(ciString(source));
        }

        LOGGER.info("GRS import sources: {}", sourcesToImport);

        for (final CIString enabledSource : sourcesToImport) {
            final GrsSource grsSource = grsSources.get(enabledSource);
            if (grsSource == null) {
                LOGGER.warn("Unknown source: {}", enabledSource);
            } else {
                Thread grsImportThread = new Thread(threadGroup, new Runnable() {
                    @Override
                    public void run() {
                        if (currentlyImporting.add(enabledSource)) {
                            try {
                                LOGGER.info("Importing: {}", enabledSource);
                                grsSourceImporter.grsImport(grsSource, rebuild);
                            } catch (RuntimeException e) {
                                grsSource.getLogger().error("Unexpected", e);
                            } finally {
                                currentlyImporting.remove(enabledSource);
                            }
                        } else {
                            grsSource.getLogger().warn("Skipped, already running");
                        }
                    }
                }, String.format("grs-import-%s", threadNum.incrementAndGet()));

                grsImportThread.setDaemon(true);
                grsImportThread.start();
            }
        }
    }
}
