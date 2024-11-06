package net.ripe.db.whois.update.handler.validator.inet6num;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.StatusDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.iptree.IpEntry;
import net.ripe.db.whois.common.iptree.Ipv6Entry;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.Inet6numStatus;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.ripe.db.whois.common.Messages.Type.ERROR;
import static net.ripe.db.whois.common.rpsl.AttributeType.STATUS;
import static net.ripe.db.whois.update.domain.Action.CREATE;

/**
 * Apply stricter status validation when creating an inet6num object.
 */
@Component
public class Inet6numStrictStatusMandatoryValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(CREATE);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.INET6NUM);

    private final StatusDao statusDao;
    private final Ipv6Tree ipv6Tree;
    private final Maintainers maintainers;

    @Autowired
    public Inet6numStrictStatusMandatoryValidator(
            final StatusDao statusDao,
            final Ipv6Tree ipv6Tree,
            final Maintainers maintainers) {
        this.statusDao = statusDao;
        this.ipv6Tree = ipv6Tree;
        this.maintainers = maintainers;
    }

    @Override
    public  List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        return validateStatusAgainstResourcesInTree(update, updateContext);
    }

    @SuppressWarnings("unchecked")
    private List<Message> validateStatusAgainstResourcesInTree(final PreparedUpdate update, final UpdateContext updateContext) {
        final List<Message> validationMessages = Lists.newArrayList();

        if (!allChildrenHaveCorrectStatus(update, validationMessages)) {
            return validationMessages;
        }

        final RpslObject updatedObject = update.getUpdatedObject();
        final Ipv6Resource ipInterval = Ipv6Resource.parse(updatedObject.getKey());

        final List<Ipv6Entry> parents = ipv6Tree.findFirstLessSpecific(ipInterval);
        if (parents.size() != 1) {
            return Arrays.asList(UpdateMessages.invalidParentEntryForInterval(ipInterval));
        }

        final Set<CIString> updateMntBy = updatedObject.getValuesForAttribute(AttributeType.MNT_BY);
        final boolean hasRsMaintainer = maintainers.isRsMaintainer(updateMntBy);

        final Inet6numStatus currentStatus = Inet6numStatus.getStatusFor(updatedObject.getValueForAttribute(STATUS));
        final Inet6numStatus parentStatus = Inet6numStatus.getStatusFor(statusDao.getStatus(parents.get(0).getObjectId()));

        if (!currentStatus.worksWithParentStatus(parentStatus, hasRsMaintainer)) {
            validationMessages.add(UpdateMessages.incorrectParentStatus(ERROR, updatedObject.getType(), parentStatus.toString()));
        }

        return validationMessages;
    }

    @SuppressWarnings("unchecked")
    private boolean allChildrenHaveCorrectStatus(final PreparedUpdate update, List<Message> validationMessages) {
        final RpslObject updatedObject = update.getUpdatedObject();
        final Inet6numStatus updatedStatus = Inet6numStatus.getStatusFor(updatedObject.getValueForAttribute(STATUS));
        final Ipv6Resource ipInterval = Ipv6Resource.parse(updatedObject.getKey());

        final List<Ipv6Entry> children = ipv6Tree.findFirstMoreSpecific(ipInterval);
        final List<Integer> childrenObjectIds = Lists.transform(children, IpEntry::getObjectId);
        final Map<Integer, CIString> childStatusMap = statusDao.getStatus(childrenObjectIds);

        for (final IpEntry child : children) {
            final Inet6numStatus childStatus = Inet6numStatus.getStatusFor(childStatusMap.get(child.getObjectId()));

            if (!childStatus.worksWithParentStatus(updatedStatus, false)) {
                validationMessages.add(UpdateMessages.incorrectChildStatus(ERROR, updatedStatus.toString(), childStatus.toString(), child.getKey().toString()));
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isSkipForOverride() {
        return true;
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
