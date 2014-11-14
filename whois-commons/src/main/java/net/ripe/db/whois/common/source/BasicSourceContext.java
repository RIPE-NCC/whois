package net.ripe.db.whois.common.source;

import net.ripe.db.whois.common.domain.CIString;

import java.util.Collection;
import java.util.Set;

public interface BasicSourceContext {

    Source getCurrentSource();
    SourceConfiguration getCurrentSourceConfiguration();

    Set<CIString> getAllSourceNames();

    SourceConfiguration getSourceConfiguration(Source source);
    Collection<SourceConfiguration> getAllSourceConfigurations();
}
