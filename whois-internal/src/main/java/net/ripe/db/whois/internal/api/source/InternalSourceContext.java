package net.ripe.db.whois.internal.api.source;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceConfiguration;
import net.ripe.db.whois.common.source.SourceContext;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;

@Component
public class InternalSourceContext implements SourceContext {
    @Override
    public Source getCurrentSource() {
        return null;
    }

    @Override
    public Set<CIString> getAllSourceNames() {
        return null;
    }

    @Override
    public Set<CIString> getGrsSourceNames() {
        return null;
    }

    @Override
    public Set<CIString> getAdditionalSourceNames() {
        return null;
    }

    @Override
    public SourceConfiguration getSourceConfiguration(Source source) {
        return null;
    }

    @Override
    public Collection<SourceConfiguration> getAllSourceConfigurations() {
        return null;
    }

    @Override
    public CIString getAlias(CIString source) {
        return null;
    }

    @Override
    public void setCurrentSourceToWhoisMaster() {

    }

    @Override
    public SourceConfiguration getCurrentSourceConfiguration() {
        return null;
    }

    @Override
    public Source getWhoisSlaveSource() {
        return null;
    }

    @Override
    public void setCurrent(Source source) {

    }

    @Override
    public void removeCurrentSource() {

    }

    @Override
    public void destroyDataSources() {

    }

    @Override
    public boolean isAcl() {
        return false;
    }

    @Override
    public boolean isMain() {
        return false;
    }

    @Override
    public boolean isVirtual() {
        return false;
    }

    @Override
    public boolean isVirtual(CIString ciString) {
        return false;
    }

    @Override
    public boolean isDummificationRequired() {
        return false;
    }

    @Override
    public boolean isTagRoutes() {
        return false;
    }
}
