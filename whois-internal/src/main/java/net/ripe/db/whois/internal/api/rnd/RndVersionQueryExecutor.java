package net.ripe.db.whois.internal.api.rnd;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.dao.VersionDao;
import net.ripe.db.whois.common.dao.VersionInfo;
import net.ripe.db.whois.common.dao.VersionLookupResult;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.source.BasicSourceContext;
import net.ripe.db.whois.query.domain.ResponseHandler;
import net.ripe.db.whois.query.domain.VersionResponseObject;
import net.ripe.db.whois.query.executor.VersionQueryExecutor;
import net.ripe.db.whois.query.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.Comparator;

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

    // Minimal implementation compared to the public data because we can rely on only having versionresponses
    @Override
    public void execute(final Query query, final ResponseHandler responseHandler) {
        final ObjectType objectType = query.getObjectTypes().iterator().next();   // internal REST API will allow only one object type
        final VersionLookupResult versionLookupResult = versionDao.findByKey(objectType, query.getSearchValue());

        ImmutableList<VersionResponseObject> versionResponseObjects = FluentIterable.from(versionLookupResult.getVersionInfos())
                .transform(new Function<VersionInfo, VersionResponseObject>() {
                    @Override
                    @NotNull
                    public VersionResponseObject apply(@NotNull VersionInfo input) {
                        return new VersionResponseObject(input.getTimestamp(), input.getOperation(), objectType, versionLookupResult.getPkey());
                    }
                })
                .toSortedList(new Comparator<VersionResponseObject>() {
                    @Override
                    public int compare(VersionResponseObject o1, VersionResponseObject o2) {
                        return o1.getDateTime().compareTo(o2.getDateTime());
                    }
                });

        for (ResponseObject message : versionResponseObjects) {
            responseHandler.handle(message);
        }
    }
}
