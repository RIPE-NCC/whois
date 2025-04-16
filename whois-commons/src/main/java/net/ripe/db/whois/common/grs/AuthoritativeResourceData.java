package net.ripe.db.whois.common.grs;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import jakarta.annotation.PostConstruct;
import net.ripe.db.whois.common.dao.ResourceDataDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.source.IllegalSourceException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Component
public class AuthoritativeResourceData {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthoritativeResourceData.class);

    private static final Splitter PROPERTY_LIST_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();

    private final ResourceDataDao resourceDataDao;

    private final Set<String> sourceNames;
    private final String source;
    private final Map<String, AuthoritativeResource> authoritativeResourceCache = Maps.newHashMap();

    @Autowired
    public AuthoritativeResourceData(@Value("${grs.sources}") final String grsSourceNames,
                                     @Value("${whois.source}") final String source,
                                     final ResourceDataDao resourceDataDao) {
        this.resourceDataDao = resourceDataDao;
        this.source = source.toLowerCase();
        this.sourceNames = PROPERTY_LIST_SPLITTER.splitToStream(grsSourceNames)
            .map(sourceName -> sourceName.toLowerCase().replace("-grs", ""))
            .collect(Collectors.toSet());
    }

    @PostConstruct
    void init() {
        refreshActiveSource();
        refreshGrsSources();
    }

    synchronized public void refreshGrsSources() {
        for (final String sourceName : sourceNames) {
            try {
                LOGGER.debug("Refresh: {}", sourceName);
                authoritativeResourceCache.put(sourceName, resourceDataDao.load(sourceName));
            } catch (RuntimeException e) {
                LOGGER.warn("Refreshing: {} failed due to {}: {}", sourceName, e.getClass().getName(), e.getMessage());
            }
        }
    }

    synchronized public void refreshActiveSource() {
        try {
            LOGGER.debug("Refresh: {}", source);
            authoritativeResourceCache.put(source, resourceDataDao.load(source));
        } catch (RuntimeException e) {
            LOGGER.warn("Refreshing on change: {} failed due to {}: {}", source, e.getClass().getName(), e.getMessage());
        }
    }

    public AuthoritativeResource getAuthoritativeResource(final CIString source) {
        final String sourceName = StringUtils.removeEnd(source.toLowerCase(), "-grs");
        final AuthoritativeResource authoritativeResource = authoritativeResourceCache.get(sourceName);
        if (authoritativeResource == null) {
            throw new IllegalSourceException(source);
        }

        return authoritativeResource;
    }

    public AuthoritativeResource getAuthoritativeResource() {
        return getAuthoritativeResource(ciString(this.source));
    }
}
