package net.ripe.db.whois.update.handler;


import com.google.common.base.Joiner;
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

import java.util.Set;

@Component
public class PendingUpdateHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PendingUpdateHandler.class);

    private final Joiner JOINER = Joiner.on(",");
    private final IdenticalPendingUpdateFinder identicalFinder;
    private final PendingUpdateDao pendingUpdateDao;
    private final Authenticator authenticator;

    @Autowired
    public PendingUpdateHandler(final IdenticalPendingUpdateFinder identicalFinder, final PendingUpdateDao pendingUpdateDao, final Authenticator authenticator) {
        this.identicalFinder = identicalFinder;
        this.pendingUpdateDao = pendingUpdateDao;
        this.authenticator = authenticator;
    }

    public void handle(final PreparedUpdate preparedUpdate, final UpdateContext updateContext) {
        final RpslObject rpslObject = preparedUpdate.getUpdatedObject();
        final PendingUpdate pendingUpdate = identicalFinder.find(rpslObject);

        Set<String> currentSuccessfuls = Sets.newHashSet(pendingUpdate.getAuthenticatedBy());
        final Set<String> passedAuthentications = updateContext.getSubject(preparedUpdate).getPassedAuthentications();
        currentSuccessfuls.addAll(passedAuthentications);

        if (!authenticator.isAuthenticationForTypeComplete(rpslObject.getType(), currentSuccessfuls)) {
            pendingUpdateDao.store(new PendingUpdate(JOINER.join(passedAuthentications), new RpslObjectBase(rpslObject.getAttributes())));
        }
        else {
            //TODO
            LOGGER.info("GOING INTO STAGE H: REMOVE PENDINGUPDATE FROM DB, PROCESS UPDATE SKIP AUTH");
        }
    }
}
