package net.ripe.db.whois.update.handler.validator.organisation;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.OrgType;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

import static net.ripe.db.whois.common.collect.CollectionHelper.uniqueResult;

@Component
public class AbuseValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.ORGANISATION);

    private final RpslObjectDao objectDao;
    private final RpslObjectUpdateDao updateDao;
    private Maintainers maintainers;

    @Autowired
    public AbuseValidator(final RpslObjectDao objectDao, final RpslObjectUpdateDao updateDao, final Maintainers maintainers) {
        this.objectDao = objectDao;
        this.maintainers = maintainers;
        this.updateDao = updateDao;
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();
        if (null == updatedObject) {
            return;
        }

        validateRemovedAbuseC(updatedObject, update, updateContext);

        if (!updatedObject.containsAttribute(AttributeType.ABUSE_C)) {
            return;
        }

        final CIString abuseC = updatedObject.getValueForAttribute(AttributeType.ABUSE_C);
        final RpslObject referencedRole = uniqueResult(objectDao.getByKeys(ObjectType.ROLE, Lists.newArrayList(abuseC)));

        if (referencedRole == null) {
            if (null != uniqueResult(objectDao.getByKeys(ObjectType.PERSON, Lists.newArrayList(abuseC)))) {
                updateContext.addMessage(update, UpdateMessages.abuseCPersonReference());
            }
        } else if (!referencedRole.containsAttribute(AttributeType.ABUSE_MAILBOX)) {
            updateContext.addMessage(update, UpdateMessages.abuseMailboxRequired(abuseC));
        }
    }

    private void validateRemovedAbuseC(final RpslObject updatedObject, final PreparedUpdate update, final UpdateContext updateContext) {
        if (hasRemovedAbuseC(updatedObject, update)) {
            boolean isAllowedToUpdate = true;
            if (OrgType.getFor(updatedObject.getValueForAttribute(AttributeType.ORG_TYPE)) == OrgType.LIR) {
                isAllowedToUpdate = false;
            }

            if (isAllowedToUpdate) {
                Collection<RpslObjectInfo> rpslObjectInfos = FluentIterable
                        .from(updateDao.getReferences(update.getReferenceObject()))
                        .filter(new Predicate<RpslObjectInfo>() {
                            @Override
                            public boolean apply(@Nullable RpslObjectInfo input) {
                                return input != null && ObjectType.RESOURCE_TYPES.contains(input.getObjectType());
                            }
                        })
                        .toList();

                for (RpslObjectInfo rpslObjectInfo : rpslObjectInfos) {
                    final RpslObject referencingObject = objectDao.getById(rpslObjectInfo.getObjectId());
                    final Set<CIString> objectMaintainers = referencingObject.getValuesForAttribute(AttributeType.MNT_BY);
                    if (!Sets.intersection(maintainers.getRsMaintainers(), objectMaintainers).isEmpty()
                            && updatedObject.getKey().equals(referencingObject.getValueForAttribute(AttributeType.ORG))) {
                        isAllowedToUpdate = false;
                        break;
                    }
                }
            }
            if (!isAllowedToUpdate) {
                updateContext.addMessage(update, UpdateMessages.abuseContactNotRemovable());
            }
        }
    }


    private boolean hasRemovedAbuseC(final RpslObject updatedObject, final PreparedUpdate update) {
        final boolean hasAbuseC = updatedObject.containsAttribute(AttributeType.ABUSE_C);

        final RpslObject referenceObject = update.getReferenceObject();
        final boolean originalHasAbuseC = null != referenceObject && referenceObject.containsAttribute(AttributeType.ABUSE_C);

        return update.getAction() == Action.MODIFY && !hasAbuseC && originalHasAbuseC;
    }

    @Override
    public ImmutableList<Action> getActions() {
        return ACTIONS;
    }

    @Override
    public ImmutableList<ObjectType> getTypes() {
        return TYPES;
    }
}
