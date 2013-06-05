package net.ripe.db.whois.update.authentication.strategy;

import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.credential.AuthenticationModule;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class MntIrtAuthentication implements AuthenticationStrategy {
    private final AuthenticationModule credentialValidators;
    private final RpslObjectDao rpslObjectDao;

    @Autowired
    MntIrtAuthentication(final AuthenticationModule credentialValidators, final RpslObjectDao rpslObjectDao) {
        this.credentialValidators = credentialValidators;
        this.rpslObjectDao = rpslObjectDao;
    }

    @Override
    public boolean supports(final PreparedUpdate update) {
        return !update.getNewValues(AttributeType.MNT_IRT).isEmpty();
    }

    @Override
    public Set<ObjectType> getPendingAuthenticationTypes() {
        return Collections.emptySet();
    }

    @Override
    public List<RpslObject> authenticate(final PreparedUpdate update, final UpdateContext updateContext) {
        final Collection<CIString> keys = update.getNewValues(AttributeType.MNT_IRT);
        final List<RpslObject> candidates = rpslObjectDao.getByKeys(ObjectType.IRT, keys);
        if (isSelfReference(update, keys)) {
            candidates.add(update.getUpdatedObject());
        }

        final List<RpslObject> authenticatedBy = credentialValidators.authenticate(update, updateContext, candidates);
        if (authenticatedBy.isEmpty()) {
            throw new AuthenticationFailedException(UpdateMessages.authenticationFailed(update.getReferenceObject(), AttributeType.MNT_IRT, candidates));
        }

        return authenticatedBy;
    }

    private boolean isSelfReference(final PreparedUpdate update, final Collection<CIString> keys) {
        return update.getType().equals(ObjectType.IRT) && keys.contains(update.getUpdatedObject().getKey());
    }
}
