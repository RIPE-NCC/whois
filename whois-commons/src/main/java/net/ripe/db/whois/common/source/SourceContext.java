package net.ripe.db.whois.common.source;

import net.ripe.db.whois.common.domain.CIString;

import java.util.Collection;
import java.util.Set;

public interface SourceContext {
    Source getCurrentSource();

    Set<CIString> getAllSourceNames();
    Set<CIString> getGrsSourceNames();
    Set<CIString> getAdditionalSourceNames();

    SourceConfiguration getSourceConfiguration(Source source);
    Collection<SourceConfiguration> getAllSourceConfigurations();

    CIString getAlias(CIString source);
    void setCurrentSourceToWhoisMaster();
    SourceConfiguration getCurrentSourceConfiguration();
    Source getWhoisSlaveSource();

    void setCurrent(Source source);

    void removeCurrentSource();

    void destroyDataSources();

    boolean isAcl();
    boolean isMain();

    boolean isVirtual();
    boolean isVirtual(CIString ciString);

    boolean isDummificationRequired();

    boolean isTagRoutes();
}
