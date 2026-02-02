package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import net.ripe.db.whois.common.domain.CIString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Component
public class GrsImporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrsImporter.class);
    private static final Splitter SOURCES_SPLITTER = Splitter.on(",").trimResults().omitEmptyStrings();

    private final GrsSourceImporter grsSourceImporter;
    private final Map<CIString, GrsSource> grsSources;
    private final AtomicInteger threadNum = new AtomicInteger();
    private final Set<CIString> currentlyImporting = Collections.synchronizedSet(Sets.<CIString>newHashSet());
    private final ThreadGroup threadGroup = new ThreadGroup("grs-import");

    private ExecutorService executorService;

    @Autowired
    public GrsImporter(final GrsSourceImporter grsSourceImporter, final GrsSource[] grsSources) {
        this.grsSourceImporter = grsSourceImporter;
        this.grsSources = Maps.newHashMapWithExpectedSize(grsSources.length);
        for (final GrsSource grsSource : grsSources) {
            this.grsSources.put(grsSource.getName(), grsSource);
        }
    }

    @PostConstruct
    void startImportThreads() {
        executorService = Executors.newFixedThreadPool(grsSources.size(), new ThreadFactory() {
            @Override
            public Thread newThread(final Runnable runnable) {
                final Thread thread = new Thread(threadGroup, runnable, String.format("grs-import-%s", threadNum.incrementAndGet()));
                thread.setDaemon(true);
                return thread;
            }
        });
    }

    @PreDestroy
    void shutdownImportThreads() {
        executorService.shutdownNow();
    }

    public List<Future> grsImport(final String sources, final boolean rebuild) {
        final Set<CIString> sourcesToImport = splitSources(sources);
        LOGGER.info("GRS import sources: {}", sourcesToImport);

        final List<Future> futures = Lists.newArrayListWithCapacity(sourcesToImport.size());
        for (final CIString enabledSource : sourcesToImport) {
            final GrsSource grsSource = grsSources.get(enabledSource);

            if (grsSource == null) {
                LOGGER.warn("Unknown source: {}", enabledSource);
                continue;
            }

            futures.add(executorService.submit(new Runnable() {
                @Override
                public void run() {
                    grsImport(grsSource, rebuild);
                }
            }));
        }

        return futures;
    }

    public void grsImport(final GrsSource source, final boolean rebuild) {
        if (!currentlyImporting.add(source.name)) {
            source.getLogger().warn("Skipped, already running");
            return;
        }

        try {
            source.getLogger().info("Importing: {}", source.name);
            grsSourceImporter.grsImport(source, rebuild);
            source.getLogger().info("Completed: {}", source.name);
        } catch (RuntimeException e) {
            source.getLogger().error("Unexpected", e);
        } finally {
            currentlyImporting.remove(source.name);
        }
    }

    private Set<CIString> splitSources(final String sources) {
        final Set<CIString> sourcesToImport = Sets.newLinkedHashSet();
        for (final String source : SOURCES_SPLITTER.split(sources)) {
            sourcesToImport.add(ciString(source));
        }
        return sourcesToImport;
    }
}
