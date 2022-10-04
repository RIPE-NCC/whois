package net.ripe.db.whois.api.healthcheck;

import net.ripe.db.whois.common.HealthCheck;
import net.ripe.db.whois.common.iptree.IpTreeCacheManager;
import net.ripe.db.whois.common.source.SourceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class IpTreeHealthCheck implements HealthCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(IpTreeHealthCheck.class);

    private final AtomicBoolean ipTreeHealthy = new AtomicBoolean(true);
    private final IpTreeCacheManager ipTreeCacheManager;
    private final SourceContext sourceContext;

    @Autowired
    public IpTreeHealthCheck(final IpTreeCacheManager ipTreeCacheManager,
                                final SourceContext sourceContext) {
        this.ipTreeCacheManager = ipTreeCacheManager;
        this.sourceContext = sourceContext;
    }

    @Override
    public boolean check() {
        return ipTreeHealthy.get();
    }

    @Scheduled(fixedDelay = 60 * 1_000)
    void updateStatus() {
        ipTreeHealthy.set(ipTreeCacheManager.check(sourceContext));
        if (!ipTreeHealthy.get()) {
            LOGGER.info("IP Tree failed health check");
        }
    }


}
