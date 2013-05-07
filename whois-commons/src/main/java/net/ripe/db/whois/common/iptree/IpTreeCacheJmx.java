package net.ripe.db.whois.common.iptree;

import net.ripe.db.whois.common.jmx.JmxBase;
import net.ripe.db.whois.common.source.SourceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.Callable;

@Component
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "IpTrees", description = "Whois IpTree maintenance")
public class IpTreeCacheJmx extends JmxBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(IpTreeCacheJmx.class);

    private final IpTreeUpdater ipTreeUpdater;
    private final IpTreeCacheManager ipTreeCacheManager;

    @Autowired
    public IpTreeCacheJmx(final IpTreeUpdater ipTreeUpdater, final IpTreeCacheManager ipTreeCacheManager) {
        super(LOGGER);
        this.ipTreeUpdater = ipTreeUpdater;
        this.ipTreeCacheManager = ipTreeCacheManager;
    }

    @ManagedOperation(description = "Initiate reload of in-memory trees")
    public String reloadTrees() {
        return invokeOperation("Reload in-memory trees", "", new Callable<String>() {
            @Override
            public String call() {
                ipTreeUpdater.rebuild();
                return "In-memory trees reloaded";
            }
        });
    }

    @ManagedOperation(description = "Initiate refresh of in-memory trees")
    public String refreshTrees() {
        return invokeOperation("Refresh in-memory trees", "", new Callable<String>() {
            @Override
            public String call() {
                ipTreeUpdater.update();
                return "In-memory trees refreshed";
            }
        });
    }

    @ManagedOperation(description = "Search maximum serial in-memory trees")
    public String getMaxSerials() {
        return invokeOperation("Find tree serials", "", new Callable<String>() {
            @Override
            public String call() {
                final StringBuilder resultBuilder = new StringBuilder();
                for (final Map.Entry<SourceConfiguration, Long> entry : ipTreeCacheManager.getLastSerials().entrySet()) {
                    resultBuilder.append(entry.getKey());
                    resultBuilder.append(':');
                    resultBuilder.append(entry.getValue());
                }

                return resultBuilder.toString();
            }
        });
    }
}
