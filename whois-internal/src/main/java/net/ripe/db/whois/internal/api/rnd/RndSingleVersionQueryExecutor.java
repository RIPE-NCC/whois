package net.ripe.db.whois.internal.api.rnd;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.VersionDao;
import net.ripe.db.whois.common.dao.VersionInfo;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.BasicSourceContext;
import net.ripe.db.whois.query.VersionDateTime;
import net.ripe.db.whois.query.executor.VersionQueryExecutor;
import net.ripe.db.whois.query.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

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


        final List<VersionInfo> versionInfos = versionDao.getVersionsBeforeTimestamp(
                query.getObjectTypes().iterator().next(), // internal REST API will allow only one object type
                query.getSearchValue(),
                query.getObjectVersion());

        if (versionInfos.isEmpty()){
            return Collections.EMPTY_LIST;
        }

        final VersionDateTime maxTimestamp = versionInfos.get(0).getTimestamp();
        final List<VersionInfo> latestVersionInfos = Lists.newArrayList(
                Iterables.filter(versionInfos, new Predicate<VersionInfo>() {
                    @Override
                    public boolean apply(@Nullable VersionInfo input) {
                        return input.getTimestamp() != maxTimestamp && input.getOperation() != Operation.DELETE;
                    }
                }));

        Collections.sort(latestVersionInfos);

        if (latestVersionInfos.isEmpty()){
            return Collections.EMPTY_LIST;
        }

        final VersionInfo last = Iterables.getLast(latestVersionInfos);

        final RpslObject rpslObject = null;
        if (last.isInLast()){
            //get from last

        } else {
            //get from history
        }

        return Collections.singletonList(new RpslObjectWithTimestamp(rpslObject, latestVersionInfos.size(), last.getTimestamp()));
    }
}
