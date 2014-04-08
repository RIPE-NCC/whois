package net.ripe.db.whois.update.handler;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.common.rpsl.attrs.AutnumStatus;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Component
public class AutnumStatusAttributeGenerator extends AttributeGenerator {
    private final Maintainers maintainers;

    @Autowired
    public AutnumStatusAttributeGenerator(final Maintainers maintainers) {
        this.maintainers = maintainers;
    }

    @Override
    public RpslObject generateAttributes(final RpslObject originalObject, final RpslObject updatedObject, final Update update, final UpdateContext updateContext) {
        switch (updatedObject.getType()) {
            case AUT_NUM:
                return generateStatus(originalObject, updatedObject, update, updateContext);
            default:
                return updatedObject;
        }
    }

    private RpslObject generateStatus(final RpslObject originalObject, final RpslObject updatedObject, final Update update, final UpdateContext updateContext) {
        if (update.getOperation() != Operation.DELETE) {
            final RpslObjectBuilder builder = new RpslObjectBuilder(updatedObject);
            if (isMaintainedByRsMaintainer(updatedObject)) {
                cleanupAttributeType(update, updateContext, builder, AttributeType.STATUS, AutnumStatus.ASSIGNED.getCIName());
            } else {
                // TODO: [ES] need more exact rules on difference between LEGACY and OTHER
                cleanupAttributeType(update, updateContext, builder, AttributeType.STATUS, AutnumStatus.OTHER.getCIName());
            }

            if (originalObject != null && originalObject.containsAttribute(AttributeType.STATUS) && !updatedObject.containsAttribute(AttributeType.STATUS)) {
                updateContext.addMessage(update, ValidationMessages.attributeCannotBeRemoved(AttributeType.STATUS));
            }

            cleanupAttributeType(update, updateContext, builder, AttributeType.REMARKS, ciString(ValidationMessages.autnumStatusRemark().getText()));

            return builder.get();
        }

        return updatedObject;
    }

    private boolean isMaintainedByRsMaintainer(final RpslObject object) {
        final Set<CIString> mntBy = object.getValuesForAttribute(AttributeType.MNT_BY);
        return !Sets.intersection(maintainers.getRsMaintainers(), mntBy).isEmpty();
    }
}
