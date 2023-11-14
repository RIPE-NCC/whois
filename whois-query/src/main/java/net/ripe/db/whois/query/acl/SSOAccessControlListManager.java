package net.ripe.db.whois.query.acl;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.domain.BlockEvent;
import net.ripe.db.whois.query.dao.SSOAccessControlListDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SSOAccessControlListManager {

    private final DateTimeProvider dateTimeProvider;
    private final SSOResourceConfiguration resourceConfiguration;
    private final SSOAccessControlListDao accessControlListDao;
    private final PersonalObjectAccounting personalObjectAccounting;

    @Autowired
    public SSOAccessControlListManager(final DateTimeProvider dateTimeProvider,
                                       final SSOResourceConfiguration resourceConfiguration,
                                       final SSOAccessControlListDao accessControlListDao,
                                       final PersonalObjectAccounting personalObjectAccounting) {
        this.dateTimeProvider = dateTimeProvider;
        this.resourceConfiguration = resourceConfiguration;
        this.accessControlListDao = accessControlListDao;
        this.personalObjectAccounting = personalObjectAccounting;
    }

    public boolean isDenied(final String ssoId) {
        return ssoId == null ? false :  resourceConfiguration.isDenied(ssoId);
    }

    public boolean canQueryPersonalObjects(final String ssoId) {
        return getPersonalObjects(ssoId) >= 0;
    }

    public int getPersonalObjects(final String ssoId) {
        final int queried = personalObjectAccounting.getQueriedPersonalObjects(ssoId);
        final int personalDataLimit = getPersonalDataLimit();

        return personalDataLimit - queried;
    }

    /**
     * Account for the ResponseObject given
     *
     * @param ssoId The ssoId .
     * @param amount        The amount of personal objects accounted.
     */
    public void accountPersonalObjects(final String ssoId, final int amount) {
        final int limit = getPersonalDataLimit();

        final int remaining = limit - personalObjectAccounting.accountPersonalObject(ssoId, amount);
        if (remaining < 0) {
            blockTemporary(ssoId, limit);
        }
    }

    public void blockTemporary(final String ssoId, final int limit) {
        accessControlListDao.saveAclEvent(ssoId, dateTimeProvider.getCurrentDate(), limit, BlockEvent.Type.BLOCK_TEMPORARY);
    }

    int getPersonalDataLimit() {
        return resourceConfiguration.getLimit();
    }
}
