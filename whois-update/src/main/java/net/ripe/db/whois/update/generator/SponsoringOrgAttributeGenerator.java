package net.ripe.db.whois.update.generator;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.springframework.stereotype.Component;

import static net.ripe.db.whois.common.rpsl.AttributeType.SPONSORING_ORG;

@Component
public class SponsoringOrgAttributeGenerator extends AttributeGenerator {

    public RpslObject generateAttributes(final RpslObject originalObject, final RpslObject updatedObject, final Update update, final UpdateContext updateContext) {
        switch (updatedObject.getType()) {
            case AUT_NUM:
            case INET6NUM:
            case INETNUM:
                return generateStatus(originalObject, updatedObject, update, updateContext);
            default:
                return updatedObject;
        }
    }

    private RpslObject generateStatus(final RpslObject originalObject, final RpslObject updatedObject, final Update update, final UpdateContext updateContext) {
        final boolean authByRS =  updateContext.getSubject(update).hasPrincipal(Principal.RS_MAINTAINER);
        final boolean isOverride =  updateContext.getSubject(update).hasPrincipal(Principal.OVERRIDE_MAINTAINER);

        if (!(authByRS || isOverride) && sponsoringOrgWasRemoved(originalObject, updatedObject)) {
            updateContext.addMessage(update, ValidationMessages.attributeValueSticky(AttributeType.SPONSORING_ORG));
            return cleanupAttributeType(update, updateContext, updatedObject, AttributeType.SPONSORING_ORG, originalObject.getValueForAttribute(SPONSORING_ORG));
        }

        return updatedObject;
    }

    private boolean sponsoringOrgWasRemoved(final RpslObject original, final RpslObject updated) {
        if (original == null) {
            return false;
        }
        final CIString refSponsoringOrg = original.getValueOrNullForAttribute(SPONSORING_ORG);
        final CIString updSponsoringOrg = updated.getValueOrNullForAttribute(SPONSORING_ORG);

        return refSponsoringOrg != null && !refSponsoringOrg.equals("") && updSponsoringOrg == null;
    }
}
