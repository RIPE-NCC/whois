package net.ripe.db.whois.nrtm.client;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.ApplicationService;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static net.ripe.db.whois.common.domain.CIString.ciString;

@Component
public class NrtmImporter implements EmbeddedValueResolverAware, ApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmImporter.class);

    private final boolean enabled;
    private final Set<CIString> sources;
    private final NrtmClientFactory nrtmClientFactory;
    private final SourceContext sourceContext;

    private StringValueResolver valueResolver;
    private ExecutorService executorService;
    private Set<NrtmClientFactory.NrtmClient> clients = Sets.newHashSet();

    @Autowired
    public NrtmImporter(final NrtmClientFactory nrtmClientFactory,
                        final SourceContext sourceContext,
                        @Value("${nrtm.import.enabled:false}") final boolean enabled,
                        @Value("${nrtm.import.sources:}") final String sources) {
        this.nrtmClientFactory = nrtmClientFactory;
        this.sourceContext = sourceContext;
        this.enabled = enabled;
        this.sources = ciSet(Splitter.on(",").trimResults().omitEmptyStrings().split(sources));
    }

    @Override
    public void setEmbeddedValueResolver(final StringValueResolver resolver) {
        this.valueResolver = resolver;
    }

    @PostConstruct
    void checkSources() {
        for (final CIString source : sources) {
            if (sourceContext.isVirtual(source)) {
                throw new IllegalArgumentException(String.format("Cannot use NRTM with virtual source: %s", source));
            }

            JdbcRpslObjectOperations.sanityCheck(sourceContext.getSourceConfiguration(Source.master(source)).getJdbcTemplate());
        }
    }

    @Override
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
                final NrtmClientFactory.NrtmClient nrtmClient = nrtmClientFactory.createNrtmClient(nrtmSource);
                clients.add(nrtmClient);
                executorService.submit(nrtmClient);
            }
        }
    }

    @Override
    public void stop() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    private List<NrtmSource> readNrtmSources() {
        final List<NrtmSource> nrtmSources = Lists.newArrayList();

        for (final CIString source : sources) {
            final String originSource = readProperty(String.format("nrtm.import.%s.source", source));
            final String host = readProperty(String.format("nrtm.import.%s.host", source));
            final String port = readProperty(String.format("nrtm.import.%s.port", source));
            try {
                nrtmSources.add(new NrtmSource(source, ciString(originSource), host, Integer.parseInt(port)));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid NRTM server port: " + port + " for source: " + source);
            }
        }

        return nrtmSources;
    }

    private String readProperty(final String name) {
        final String value = valueResolver.resolveStringValue(String.format("${%s}", name));
        if (value.equals(name)) {
            throw new IllegalArgumentException("Property " + name + " is not defined.");
        }

        return value;
    }

}
