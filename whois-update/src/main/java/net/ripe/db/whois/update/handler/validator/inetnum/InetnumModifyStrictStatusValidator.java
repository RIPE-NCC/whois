package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.StatusDao;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.attrs.InetnumStatus;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import static net.ripe.db.whois.update.domain.Action.MODIFY;

/**
 * Apply stricter status validation when modifying an inetnum object.
 */
@Component
public class InetnumModifyStrictStatusValidator extends InetnumStrictStatusValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.INETNUM);

    @Autowired
    public InetnumModifyStrictStatusValidator(
            final RpslObjectDao objectDao,
            final StatusDao statusDao,
            final Ipv4Tree ipv4Tree,
            final Maintainers maintainers) {
        super(objectDao, statusDao, ipv4Tree, maintainers);

    }

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        if(canSkipValidation(update)) {
            return Collections.EMPTY_LIST;
        }

        return validateStatusAgainstResourcesInTree(update, updateContext);
    }

    protected static boolean canSkipValidation(final PreparedUpdate update) {
        final InetnumStatus originalStatus = InetnumStatus.getStatusFor(update.getReferenceObject().getValueForAttribute(AttributeType.STATUS));
        final InetnumStatus updateStatus = InetnumStatus.getStatusFor(update.getUpdatedObject().getValueForAttribute(AttributeType.STATUS));

        return (originalStatus == updateStatus) || !InetnumStatusValidator.canChangeStatus(originalStatus, updateStatus);
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
