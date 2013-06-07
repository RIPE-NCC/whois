package net.ripe.db.whois.update.handler;


import com.google.common.collect.Sets;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBase;
import net.ripe.db.whois.update.authentication.Authenticator;
import net.ripe.db.whois.update.dao.PendingUpdateDao;
import net.ripe.db.whois.update.domain.PendingUpdate;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.CheckForNull;
import java.util.List;
import java.util.Set;

@Component
class PendingUpdateHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PendingUpdateHandler.class);

    private final PendingUpdateDao pendingUpdateDao;
    private final Authenticator authenticator;

    @Autowired
    public PendingUpdateHandler(final PendingUpdateDao pendingUpdateDao, final Authenticator authenticator) {
        this.pendingUpdateDao = pendingUpdateDao;
        this.authenticator = authenticator;
    }

    public void handle(final PreparedUpdate preparedUpdate, final UpdateContext updateContext) {
        final RpslObject rpslObject = preparedUpdate.getUpdatedObject();
        final PendingUpdate pendingUpdate = find(rpslObject);
        final Set<String> passedAuthentications = updateContext.getSubject(preparedUpdate).getPassedAuthentications();

        if (pendingUpdate == null) {
            pendingUpdateDao.store(new PendingUpdate(passedAuthentications, new RpslObjectBase(rpslObject.getAttributes())));
        }
        else {
            Set<String> currentSuccessfuls = Sets.newHashSet(pendingUpdate.getPassedAuthentications());
            currentSuccessfuls.addAll(passedAuthentications);

            if (authenticator.isAuthenticationForTypeComplete(rpslObject.getType(), currentSuccessfuls)) {
                //TODO
                LOGGER.info("GOING INTO STAGE H: REMOVE PENDINGUPDATE FROM DB, PROCESS UPDATE SKIP AUTH");
            } else {
                pendingUpdateDao.store(new PendingUpdate(passedAuthentications, new RpslObjectBase(rpslObject.getAttributes())));
            }
        }
    }

    @CheckForNull
    private PendingUpdate find(final RpslObject object) {
        final List<PendingUpdate> result = pendingUpdateDao.findByTypeAndKey(object.getType(), object.getKey().toString());
        for (final PendingUpdate update : result) {
            final RpslObjectBase objectBase = update.getObject();

            if (objectBase.getAttributes().size() == object.getAttributes().size() &&
                    objectBase.getAttributes().containsAll(object.getAttributes())) {
                return update;
            }
        }
        return null;
    }
}
