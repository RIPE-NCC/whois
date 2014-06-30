package net.ripe.db.whois.update.handler.validator.organisation;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.ripe.db.whois.common.collect.CollectionHelper.uniqueResult;

@Component
public class AbuseValidator implements BusinessRuleValidator {

    private final RpslObjectDao objectDao;
    private Maintainers maintainers;

    @Autowired
    public AbuseValidator(final RpslObjectDao objectDao, final Maintainers maintainers) {
        this.objectDao = objectDao;
        this.maintainers = maintainers;
    }

    @Override
    public List<Action> getActions() {
        return Lists.newArrayList(Action.CREATE, Action.MODIFY);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Lists.newArrayList(ObjectType.ORGANISATION);
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
                        .from(objectDao.relatedTo(update.getReferenceObject(), new HashSet<ObjectType>()))
                        .filter(new Predicate<RpslObjectInfo>() {
                            @Override
                            public boolean apply(@Nullable RpslObjectInfo input) {
                                return input != null && input.getObjectType().isResourceType();
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
}
