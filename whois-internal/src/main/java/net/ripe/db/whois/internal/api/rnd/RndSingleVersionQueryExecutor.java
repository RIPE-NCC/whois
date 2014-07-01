package net.ripe.db.whois.internal.api.rnd;

import net.ripe.db.whois.common.dao.VersionDao;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.BasicSourceContext;
import net.ripe.db.whois.query.executor.VersionQueryExecutor;
import net.ripe.db.whois.query.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class RndSingleVersionQueryExecutor extends VersionQueryExecutor {

    @Autowired
    public RndSingleVersionQueryExecutor (final BasicSourceContext sourceContext,  final VersionDao versionDao) {
        super(sourceContext, versionDao);
    }

    @Override
    public boolean isAclSupported() {
        return false;
    }

    @Override
    public boolean supports(Query query) {
        return query.isObjectVersion();
    }

    @Override
    protected Iterable<? extends ResponseObject> getResponseObjects(final Query query) {

        final RpslObject rpslObject = versionDao.findHistoricalObject(
                query.getObjectTypes().iterator().next(), // internal REST API will allow only one object type
                query.getSearchValue(),
                query.getObjectVersion());

        if (rpslObject != null) {
            return Collections.singletonList(rpslObject);
        } else {
            return Collections.EMPTY_LIST;
        }
    }
}
