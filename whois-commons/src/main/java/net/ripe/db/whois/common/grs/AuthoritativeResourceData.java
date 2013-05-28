package net.ripe.db.whois.common.grs;

import com.google.common.collect.Maps;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.io.Downloader;
import net.ripe.db.whois.common.source.IllegalSourceException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static net.ripe.db.whois.common.domain.CIString.ciSet;

@Component
public class AuthoritativeResourceData implements EmbeddedValueResolverAware {
    private final static Logger LOGGER = LoggerFactory.getLogger(AuthoritativeResourceData.class);
    private final static int DAILY_MS = 24 * 60 * 60 * 1000;

    private final Downloader downloader;
    private final String downloadDir;
    private StringValueResolver valueResolver;

    private final Set<CIString> sources;
    private final Map<CIString, AuthoritativeResource> authoritativeResourceCache = Maps.newHashMap();
    private Timer timer;

    @Autowired
    public AuthoritativeResourceData(
            @Value("${grs.sources}") final List<String> sources,
            @Value("${dir.grs.import.download:}") final String downloadDir,
            final Downloader downloader) {
        this.sources = ciSet(sources);
        this.downloadDir = downloadDir;
        this.downloader = downloader;
    }

    @Override
    public void setEmbeddedValueResolver(final StringValueResolver valueResolver) {
        this.valueResolver = valueResolver;
    }

    @PostConstruct
    void init() {
        for (final CIString source : sources) {
            authoritativeResourceCache.put(source, loadAuthoritativeResource(source));
        }

        timer = new Timer(String.format("%s-refresh", getClass().getSimpleName()), true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                refreshAuthoritativeResourceCache();
            }
        }, DAILY_MS, DAILY_MS);
    }

    @PreDestroy
    void cleanup() {
        timer.cancel();
    }

    void refreshAuthoritativeResourceCache() {
        for (final CIString source : sources) {
            try {
                LOGGER.debug("Refresh: {}", source);
                authoritativeResourceCache.put(source, loadAuthoritativeResource(source));
            } catch (RuntimeException e) {
                LOGGER.error("Refreshing: {}", source, e);
            }
        }
    }

    public AuthoritativeResource getAuthoritativeResource(final CIString source) {
        final AuthoritativeResource authoritativeResource = authoritativeResourceCache.get(source);
        if (authoritativeResource == null) {
            throw new IllegalSourceException(source);
        }

        return authoritativeResource;
    }

    private AuthoritativeResource loadAuthoritativeResource(final CIString source) {
        final Logger logger = LoggerFactory.getLogger(String.format("%s_%s", getClass().getName(), source));
        final String sourceName = source.toLowerCase().replace("-grs", "");
        final String propertyName = String.format("${grs.import.%s.resourceDataUrl:}", sourceName);
        final String resourceDataUrl = valueResolver.resolveStringValue(propertyName);
        if (StringUtils.isBlank(resourceDataUrl)) {
            return AuthoritativeResource.unknown(logger);
        }

        final File resourceDataDownload = new File(downloadDir, source + "-RES.tmp");
        final File resourceDataFile = new File(downloadDir, source + "-RES");
        try {
            downloader.downloadGrsData(logger, new URL(resourceDataUrl), resourceDataDownload);

            if (resourceDataFile.exists()) {
                if (resourceDataFile.delete()) {
                    if (!resourceDataDownload.renameTo(resourceDataFile)) {
                        logger.warn("Unable to rename downloaded resource data file: {}", resourceDataFile.getAbsolutePath());
                    }
                } else {
                    logger.warn("Unable to delete previous resource data file: {}", resourceDataFile.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            logger.warn("Download {} failed: {}", source, resourceDataUrl, e);
        }

        return AuthoritativeResource.loadFromFile(logger, sourceName, resourceDataFile);
    }
}
