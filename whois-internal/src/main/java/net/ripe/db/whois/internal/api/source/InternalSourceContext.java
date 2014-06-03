package net.ripe.db.whois.internal.api.source;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.source.BasicSourceContext;
import net.ripe.db.whois.common.source.IllegalSourceException;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciString;

/**
 * For the internal API, for the moment we keep it simple and only use the whois readonly slave
 * Only use a subset of the complete SourceContext interface, add additional functionality when needed
 * When full functionality needed replace BasicSourceContext with SourceContext
 */
@Component
public class InternalSourceContext implements BasicSourceContext {

    private final CIString mainSourceName;
    private final Source mainSource;
    private final SourceConfiguration mainSourceConfiguration;

    @Autowired
    public InternalSourceContext(@Value("${whois.source}") final String mainSourceNameString,
                                 @Qualifier("whoisReadOnlySlaveDataSource") final DataSource mainDataSource) {
        this.mainSourceName = ciString(mainSourceNameString);
        this.mainSource = Source.slave(mainSourceName);
        this.mainSourceConfiguration = new SourceConfiguration(mainSource, mainDataSource);
    }

    @Override
    public Source getCurrentSource() {
        return mainSource;
    }

    @Override
    public SourceConfiguration getCurrentSourceConfiguration() {
        return mainSourceConfiguration;
    }

    @Override
    public Set<CIString> getAllSourceNames() {
        return Sets.newLinkedHashSet(Collections.singleton(mainSourceName));
    }

    @Override
    public SourceConfiguration getSourceConfiguration(Source source) {
        if (source.getName().equals(mainSourceName)) {
            return mainSourceConfiguration;
        } else {
            throw new IllegalSourceException(source.getName());
        }
    }

    @Override
    public Collection<SourceConfiguration> getAllSourceConfigurations() {
        return Collections.singleton(mainSourceConfiguration);
    }
}
