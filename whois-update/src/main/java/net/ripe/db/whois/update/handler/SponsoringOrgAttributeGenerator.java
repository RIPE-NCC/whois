package net.ripe.db.whois.update.handler;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
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
            final RpslObjectBuilder builder = new RpslObjectBuilder(updatedObject);

            final CIString originalSponsoringOrgValue = originalObject.getValueOrNullForAttribute(SPONSORING_ORG);
            cleanupAttributeType(update, updateContext, builder, AttributeType.SPONSORING_ORG, originalSponsoringOrgValue);
            updateContext.addMessage(update, ValidationMessages.attributeValueSticky(AttributeType.SPONSORING_ORG));
            return builder.get();
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
