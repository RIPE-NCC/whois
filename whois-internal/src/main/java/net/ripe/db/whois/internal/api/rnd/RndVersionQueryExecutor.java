package net.ripe.db.whois.internal.api.rnd;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.VersionDao;
import net.ripe.db.whois.common.dao.VersionInfo;
import net.ripe.db.whois.common.dao.VersionLookupResult;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.transform.FilterAuthFunction;
import net.ripe.db.whois.common.rpsl.transform.FilterEmailFunction;
import net.ripe.db.whois.common.source.BasicSourceContext;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.domain.DeletedVersionResponseObject;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.ResponseHandler;
import net.ripe.db.whois.query.domain.VersionResponseObject;
import net.ripe.db.whois.query.domain.VersionWithRpslResponseObject;
import net.ripe.db.whois.query.executor.VersionQueryExecutor;
import net.ripe.db.whois.query.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static net.ripe.db.whois.common.domain.serials.Operation.DELETE;

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

    @Override
    // TODO [FRV]: There is going to be ton of code duplication here with the public version. Seperate this out and move to protected base class
    public void execute(Query query, ResponseHandler responseHandler) {
        ObjectType objectType = query.getObjectTypes().iterator().next();   // internal REST API will allow only one object type

        VersionLookupResult versionLookupResult = versionDao.findByKey(objectType, query.getSearchValue());

        // transform the version lookup results in an array of ResponsObjects
        final List<ResponseObject> messages = Lists.newArrayList();

        int versionIndex = 1;
        int versionPadding = 0;    // not relevant for REST API
        for (VersionInfo versionInfo : versionLookupResult.getVersionInfos()) {
            if (versionInfo.getOperation() == DELETE) {
                messages.add(new DeletedVersionResponseObject(versionInfo.getTimestamp(), objectType, versionLookupResult.getPkey()));
            } else {
                messages.add(new VersionResponseObject(versionPadding, versionInfo.getOperation(), versionIndex, versionInfo.getTimestamp(), objectType, versionLookupResult.getPkey()));

            }
        }

        for (ResponseObject message : decorate(query, messages))  {
            responseHandler.handle(message);
        }
    }

    private Iterable<? extends ResponseObject> decorate(final Query query, Iterable<? extends ResponseObject> responseObjects) {
        return Iterables.transform(responseObjects, new Function<ResponseObject, ResponseObject>() {
            @Override
            public ResponseObject apply(ResponseObject input) {
                if (input instanceof RpslObject) {
                    input = (new FilterEmailFunction()).apply((new FilterAuthFunction()).apply((RpslObject) input));
                    // TODO [FRV]: THis seems to be specific to get one responseobject version. Part of other story, remove for the moment
                    /*
                    if (query.isObjectVersion()) {
                        filtered = new VersionWithRpslResponseObject((RpslObject) filtered, query.getObjectVersion());
                    }
                    */
                }
                return input;
            }
        });
    }
}
