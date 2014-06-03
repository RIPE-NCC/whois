package net.ripe.db.whois.internal.api.rnd;

import net.ripe.db.whois.common.dao.VersionDao;
import net.ripe.db.whois.common.source.BasicSourceContext;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.executor.VersionQueryExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RndVersionQueryExecutor extends VersionQueryExecutor {

    @Autowired
    public RndVersionQueryExecutor(final BasicSourceContext sourceContext, final VersionDao versionDao) {
        super(sourceContext, versionDao);
    }

    @Override
    public boolean isAclSupported() {
        return false;
    }
}
