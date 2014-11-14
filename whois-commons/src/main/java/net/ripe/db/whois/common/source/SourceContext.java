package net.ripe.db.whois.common.source;

import net.ripe.db.whois.common.domain.CIString;

import java.util.Collection;
import java.util.Set;

public interface SourceContext extends BasicSourceContext {

    Set<CIString> getGrsSourceNames();
    Set<CIString> getAdditionalSourceNames();

    CIString getAlias(CIString source);
    void setCurrentSourceToWhoisMaster();
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
