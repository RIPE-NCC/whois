package net.ripe.db.nrtm4.source;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mchange.v2.c3p0.DataSources;
import jakarta.annotation.PreDestroy;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.source.IllegalSourceException;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceConfiguration;
import net.ripe.db.whois.common.source.SourceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.CheckForNull;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Component
public class NrtmSourceContext implements SourceContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmSourceContext.class);
    private final CIString mainSourceName;
    private final Source mainMasterSource;
    private final Source mainSlaveSource;
    private final Source mainNonAuthSource;

    private final String nonauthRipeSourceNameString;

    private final Map<Source, SourceConfiguration> sourceConfigurations = Maps.newLinkedHashMap();

    private final Set<CIString> allSourceNames;
    private final Map<CIString, CIString> aliases;

    private ThreadLocal<SourceConfiguration> current = new ThreadLocal<>();

    @Autowired
    public NrtmSourceContext(
            @Value("${whois.source}") final String mainSourceNameString,
            @Value("${whois.nonauth.source}") final String nonauthRipeSourceNameString,
            @Qualifier("nrtmMasterDataSource") final DataSource nrtmMasterDataSource,
            @Qualifier("nrtmSlaveDataSource") final DataSource whoisSlaveDataSource) {

        mainSourceName = ciString(mainSourceNameString);
        this.mainMasterSource = Source.master(mainSourceName);
        this.mainSlaveSource = Source.slave(mainSourceName);

        this.nonauthRipeSourceNameString = nonauthRipeSourceNameString;
        this.mainNonAuthSource = Source.master(this.nonauthRipeSourceNameString);

        final Map<CIString, CIString> newAliases = Maps.newLinkedHashMap();

        sourceConfigurations.put(mainMasterSource, new SourceConfiguration(mainMasterSource, nrtmMasterDataSource));
        sourceConfigurations.put(mainSlaveSource, new SourceConfiguration(mainSlaveSource, whoisSlaveDataSource));

        LOGGER.info("Using sources: {}", sourceConfigurations.keySet());

        this.aliases = Collections.unmodifiableMap(newAliases);
        this.allSourceNames = Collections.unmodifiableSet(Sets.newLinkedHashSet(Iterables.transform(sourceConfigurations.keySet(), source -> source.getName())));
    }

    public SourceConfiguration getCurrentSourceConfiguration() {
        final SourceConfiguration sourceConfiguration = current.get();
        if (sourceConfiguration == null) {
            return sourceConfigurations.get(mainSlaveSource);
        }

        return sourceConfiguration;
    }

    public Collection<SourceConfiguration> getAllSourceConfigurations() {
        return sourceConfigurations.values();
    }

    public SourceConfiguration getSourceConfiguration(final Source source) {
        final SourceConfiguration sourceConfiguration = sourceConfigurations.get(source);
        if (sourceConfiguration == null) {
            throw new IllegalSourceException(source.toString());
        }

        return sourceConfiguration;
    }

    @PreDestroy
    public void destroyDataSources() {
        for (final SourceConfiguration sourceConfig : sourceConfigurations.values()) {
            try {
                DataSources.destroy(sourceConfig.getDataSource());
            } catch (SQLException e) {
                LOGGER.error("Destroying datasource", e);
            }
        }
    }

    @Override
    public Set<CIString> getAllSourceNames() {
        return allSourceNames;
    }

    public Set<CIString> getGrsSourceNames() {
        return Sets.newHashSet();
    }

    public Set<CIString> getAdditionalSourceNames() {
        return Sets.newHashSet();
    }

    @CheckForNull
    public CIString getAlias(final CIString source) {
        return aliases.get(source);
    }

    public void setCurrentSourceToWhoisMaster() {
        setCurrent(mainMasterSource);
    }

    public void setCurrent(final Source source) {
        if (this.nonauthRipeSourceNameString.equals(source.getName().toString())) {
            setCurrentSourceToWhoisMaster();
        } else {
            final SourceConfiguration sourceConfiguration = sourceConfigurations.get(source);
            if (sourceConfiguration == null) {
                throw new IllegalSourceException(source.getName().toString());
            }
            current.set(sourceConfiguration);
        }
    }

    public Source getSlaveSource() {
        return mainSlaveSource;
    }

    public Source getMasterSource() {
        return mainMasterSource;
    }
    public Source getNonauthSource() {
        return mainNonAuthSource;
    }

    @Override
    public Source getCurrentSource() {
        return getCurrentSourceConfiguration().getSource();
    }

    public void removeCurrentSource() {
        current.remove();
    }

    public boolean isAcl() {
        return false;
    }

    public boolean isMain() {
        return getCurrentSource().getName().equals(mainSourceName);
    }

    // source: NONAUTH are placed in RIPE source to represent object out of region
    public boolean isOutOfRegion(String source) {
        return source.equalsIgnoreCase(nonauthRipeSourceNameString);
    }

    public boolean isVirtual() {
        return isVirtual(getCurrentSource().getName());
    }

    public boolean isVirtual(final CIString source) {
        return aliases.containsKey(source);
    }

    public boolean isDummificationRequired() {
        return false;
    }

    public boolean isTagRoutes() {
        return false;
    }
}
