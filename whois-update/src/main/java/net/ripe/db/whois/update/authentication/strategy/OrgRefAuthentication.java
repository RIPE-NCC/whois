package net.ripe.db.whois.update.authentication.strategy;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Message;
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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class OrgRefAuthentication extends AuthenticationStrategyBase {

    private final AuthenticationModule credentialValidators;
    private final RpslObjectDao rpslObjectDao;

    @Autowired
    public OrgRefAuthentication(final AuthenticationModule credentialValidators, final RpslObjectDao rpslObjectDao) {
        this.credentialValidators = credentialValidators;
        this.rpslObjectDao = rpslObjectDao;
    }

    @Override
    public boolean supports(final PreparedUpdate update) {
        return !update.getNewValues(AttributeType.ORG).isEmpty();
    }

    @Override
    public List<RpslObject> authenticate(final PreparedUpdate update, final UpdateContext updateContext) {
        final List<Message> authenticationMessages = Lists.newArrayList();

        final Map<RpslObject, List<RpslObject>> candidatesForOrganisations = getCandidatesForOrganisations(update, updateContext);
        final Set<RpslObject> authenticatedOrganisations = Sets.newLinkedHashSet();
        for (final Map.Entry<RpslObject, List<RpslObject>> organisationEntry : candidatesForOrganisations.entrySet()) {
            final List<RpslObject> candidates = organisationEntry.getValue();
            final List<RpslObject> authenticatedBy = credentialValidators.authenticate(update, updateContext, candidates);

            if (authenticatedBy.isEmpty()) {
                final RpslObject organisation = organisationEntry.getKey();
                authenticationMessages.add(UpdateMessages.authenticationFailed(organisation, AttributeType.MNT_REF, candidates));
            } else {
                authenticatedOrganisations.addAll(authenticatedBy);
            }
        }

        if (!authenticationMessages.isEmpty()) {
            throw new AuthenticationFailedException(authenticationMessages, Sets.newLinkedHashSet(Iterables.concat(candidatesForOrganisations.values())));
        }

        return Lists.newArrayList(authenticatedOrganisations);
    }

    private boolean isSelfReference(final PreparedUpdate update, final Collection<CIString> newOrgReferences) {
        return update.getType().equals(ObjectType.ORGANISATION) && newOrgReferences.contains(update.getUpdatedObject().getKey());
    }

    private Map<RpslObject, List<RpslObject>> getCandidatesForOrganisations(final PreparedUpdate update, final UpdateContext updateContext) {
        final Map<RpslObject, List<RpslObject>> candidates = new LinkedHashMap<>();

        final Collection<CIString> newOrgReferences = update.getNewValues(AttributeType.ORG);
        final List<RpslObject> organisations = rpslObjectDao.getByKeys(ObjectType.ORGANISATION, newOrgReferences);
        if (isSelfReference(update, newOrgReferences)) {
            organisations.add(update.getUpdatedObject());
        }

        for (final RpslObject organisation : organisations) {
            final List<RpslObject> maintainers = Lists.newArrayList();

            for (final CIString mntRef : organisation.getValuesForAttribute(AttributeType.MNT_REF)) {
                try {
                    maintainers.add(rpslObjectDao.getByKey(ObjectType.MNTNER, mntRef.toString()));
                } catch (EmptyResultDataAccessException e) {
                    updateContext.addMessage(update, UpdateMessages.nonexistantMntRef(organisation.getKey(), mntRef));
                }
            }

            candidates.put(organisation, maintainers);
        }

        return candidates;
    }
}
