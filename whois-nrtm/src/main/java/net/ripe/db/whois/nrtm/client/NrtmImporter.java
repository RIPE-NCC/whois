package net.ripe.db.whois.nrtm.client;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class NrtmImporter implements EmbeddedValueResolverAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmImporter.class);

    private static final Splitter SOURCES_SPLITTER = Splitter.on(",").trimResults().omitEmptyStrings();

    private final boolean enabled;
    private final String sources;
    private final NrtmClient nrtmClient;

    private StringValueResolver valueResolver;
    private ExecutorService executorService;

    @Autowired
    public NrtmImporter(final NrtmClient nrtmClient,
                        @Value("${nrtm.import.enabled:false}") final boolean enabled,
                        @Value("${nrtm.import.sources:}") final String sources) {
        this.nrtmClient = nrtmClient;
        this.enabled = enabled;
        this.sources = sources;
    }

    @Override
    public void setEmbeddedValueResolver(final StringValueResolver resolver) {
        this.valueResolver = resolver;
    }

    @PostConstruct
    public void start() {
        if (!enabled) {
            return;
        }

        final List<NrtmSource> nrtmSources = readNrtmSources();

        if (nrtmSources.size() > 0) {
            LOGGER.info("Initializing thread pool with {} thread(s)", nrtmSources.size());

            executorService = Executors.newFixedThreadPool(nrtmSources.size(), new ThreadFactory() {
                final ThreadGroup threadGroup = new ThreadGroup(Thread.currentThread().getThreadGroup(), "NrtmClients");
                final AtomicInteger threadNum = new AtomicInteger();

                @Override
                public Thread newThread(final Runnable r) {
                    return new Thread(threadGroup, r, String.format("NrtmClient-%s", threadNum.incrementAndGet()));
                }
            });

            for (NrtmSource nrtmSource : nrtmSources) {
                executorService.submit(nrtmClient.start(nrtmSource.getName(), nrtmSource.getHost(), nrtmSource.getPort()));
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    private List<NrtmSource> readNrtmSources() {
        final List<NrtmSource> nrtmSources = Lists.newArrayList();

        for (String source : SOURCES_SPLITTER.split(this.sources)) {
            final String host = readHostProperty(source);
            final String port = readPortProperty(source);
            try {
                nrtmSources.add(new NrtmSource(source, host, Integer.parseInt(port)));
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Invalid NRTM server port: " + port + " for source: " + source);
            }
        }

        return nrtmSources;
    }

    private String readHostProperty(final String sourceName) {
        return readProperty(String.format("nrtm.import.%s.host", sourceName));
    }

    private String readPortProperty(final String sourceName) {
        return readProperty(String.format("nrtm.import.%s.port", sourceName));
    }

    private String readProperty(final String name) {
        final String value = valueResolver.resolveStringValue(String.format("${%s}", name));
        if (value.equals(name)) {
            throw new IllegalStateException("Property " + name + " is not defined.");
        }

        return value;
    }

    public class NrtmSource {
        private final String name;
        private final String host;
        private final int port;

        public NrtmSource(final String name, final String host, final int port) {
            this.name = name;
            this.host = host;
            this.port = port;
        }

        public String getName() {
            return name;
        }
         public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }
    }
}
