package net.ripe.db.whois.common.iptree;

import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceConfiguration;
import net.ripe.db.whois.common.source.SourceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class IpTreeUpdater {
    private static final Logger LOGGER = LoggerFactory.getLogger(IpTreeUpdater.class);

    public static final int TREE_UPDATE_IN_SECONDS = 10;

    private final IpTreeCacheManager ipTreeCacheManager;

    private SourceContext sourceContext;
    private Set<SourceConfiguration> sourceConfigurationsForRebuild;
    private Set<SourceConfiguration> sourceConfigurationsForUpdate;

    private ExecutorService executorService;

    @Autowired
    public IpTreeUpdater(final IpTreeCacheManager ipTreeCacheManager) {
        this.ipTreeCacheManager = ipTreeCacheManager;
    }

    @Autowired(required = false)
    void setSourceContext(final SourceContext sourceContext) {
        this.sourceContext = sourceContext;

        sourceConfigurationsForRebuild = getSourceConfigurationsWithTypePreference(sourceContext, Source.Type.SLAVE);
        LOGGER.info("Rebuild IpTrees using sources: {}", sourceConfigurationsForRebuild);

        sourceConfigurationsForUpdate = getSourceConfigurationsWithTypePreference(sourceContext, Source.Type.MASTER);
        LOGGER.info("Update IpTrees using sources: {}", sourceConfigurationsForUpdate);
    }

    private Set<SourceConfiguration> getSourceConfigurationsWithTypePreference(final SourceContext sourceContext, final Source.Type preferredType) {
        final Map<CIString, SourceConfiguration> sourceConfigurationMap = Maps.newHashMap();
        for (final SourceConfiguration sourceConfiguration : sourceContext.getAllSourceConfigurations()) {
            final CIString alias = sourceContext.getAlias(sourceConfiguration.getSource().getName());
            if (alias != null) {
                LOGGER.info("Delegating requests for {} to {}", sourceConfiguration.getSource().getName(), alias);
                continue;
            }

            final SourceConfiguration existingSourceConfiguration = sourceConfigurationMap.get(sourceConfiguration.getSource().getName());
            if (existingSourceConfiguration == null || sourceConfiguration.getSource().getType().equals(preferredType)) {
                sourceConfigurationMap.put(sourceConfiguration.getSource().getName(), sourceConfiguration);
            }
        }

        return Sets.newLinkedHashSet(sourceConfigurationMap.values());
    }

    @PostConstruct
    public void init() {
        final int nrThreads = sourceConfigurationsForRebuild.size();
        LOGGER.info("Initializing thread pool with {} threads", nrThreads);
        executorService = Executors.newFixedThreadPool(nrThreads, new ThreadFactory() {
            final ThreadGroup threadGroup = new ThreadGroup(Thread.currentThread().getThreadGroup(), "IpTreeUpdater");
            final AtomicInteger threadNum = new AtomicInteger();

            @Override
            public Thread newThread(final Runnable r) {
                return new Thread(threadGroup, r, String.format("IpTreeUpdater-%s", threadNum.incrementAndGet()));
            }
        });

        rebuild();
    }

    @PreDestroy
    public void stop() {
        executorService.shutdownNow();
    }

    public void rebuild() {
        LOGGER.info("Building IP trees");
        final Stopwatch stopwatch = Stopwatch.createStarted();

        invokeAll(sourceConfigurationsForRebuild, new OperationCallback() {
            @Override
            public void execute(final SourceConfiguration sourceConfiguration) {
                ipTreeCacheManager.rebuild(sourceConfiguration);
            }
        });

        LOGGER.info("Finished building IP trees in {}", stopwatch);
    }

    public void rebuild(final String source) {
        for (SourceConfiguration sourceConfiguration : Iterables.filter(sourceConfigurationsForRebuild, new Predicate<SourceConfiguration>() {
            @Override
            public boolean apply(final SourceConfiguration input) {
                return input.getSource().getName().contains(source);
            }
        })) {
            LOGGER.info("Rebuilding IP trees for {}", sourceConfiguration);
            final Stopwatch stopwatch = Stopwatch.createStarted();
            ipTreeCacheManager.rebuild(sourceConfiguration);
            LOGGER.info("Finished building IP trees for {} in {}", sourceConfiguration, stopwatch);
        }
    }

    @Scheduled(fixedDelay = TREE_UPDATE_IN_SECONDS * 1000)
    public void update() {
        invokeAll(sourceConfigurationsForUpdate, new OperationCallback() {
            @Override
            public void execute(final SourceConfiguration sourceConfiguration) {
                ipTreeCacheManager.update(sourceConfiguration);
            }
        });
    }

    public void updateCurrent() {
        ipTreeCacheManager.update(sourceContext.getCurrentSourceConfiguration());
    }

    private void invokeAll(final Set<SourceConfiguration> sourceConfigurations, final OperationCallback operationCallback) {
        final List<Future<?>> futures = Lists.newArrayList();
        for (final SourceConfiguration sourceConfiguration : sourceConfigurations) {
            final Future<?> future = executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        operationCallback.execute(sourceConfiguration);
                    } catch (RuntimeException e) {
                        LOGGER.error("Unexpected", e);
                    }
                }
            });

            futures.add(future);
        }

        for (final Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                LOGGER.warn("Interrupted", e);
            } catch (ExecutionException e) {
                LOGGER.warn("Execution failed", e);
            }
        }
    }

    interface OperationCallback {
        void execute(SourceConfiguration sourceConfiguration);
    }
}
