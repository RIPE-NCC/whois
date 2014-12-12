package net.ripe.db.whois.update.authentication.strategy;


import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.credential.AuthenticationModule;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class AutnumAuthentication extends AuthenticationStrategyBase {
    private final RpslObjectDao objectDao;
    private final AuthenticationModule authenticationModule;

    @Autowired
    public AutnumAuthentication(final RpslObjectDao objectDao, final AuthenticationModule authenticationModule) {
        this.objectDao = objectDao;
        this.authenticationModule = authenticationModule;
    }

    @Override
    public boolean supports(final PreparedUpdate update) {
        return update.getType().equals(ObjectType.AUT_NUM) && update.getAction().equals(Action.CREATE);
    }

    @Override
    public List<RpslObject> authenticate(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject object = update.getUpdatedObject();

        final CIString pkey = object.getKey();
        final long number = Long.parseLong(pkey.toString().substring("AS".length()));
        final RpslObject asBlock = objectDao.findAsBlock(number, number);
        if (asBlock == null) {
            throw new AuthenticationFailedException(UpdateMessages.noParentAsBlockFound(pkey), Collections.<RpslObject>emptyList());
        }

        AttributeType attributeType = AttributeType.MNT_LOWER;
        Collection<CIString> parentMnts = asBlock.getValuesForAttribute(attributeType);
        if (parentMnts.isEmpty()) {
            attributeType = AttributeType.MNT_BY;
            parentMnts = asBlock.getValuesForAttribute(attributeType);
        }

        final List<RpslObject> maintainers = objectDao.getByKeys(ObjectType.MNTNER, parentMnts);
        final List<RpslObject> authenticatedBy = authenticationModule.authenticate(update, updateContext, maintainers);
        if (authenticatedBy.isEmpty()) {
            throw new AuthenticationFailedException(UpdateMessages.authenticationFailed(asBlock, attributeType, maintainers), maintainers);
        }
        return authenticatedBy;
    }
}
