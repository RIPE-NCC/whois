package net.ripe.db.whois.update.generator;

import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.common.rpsl.AttributeTemplate;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.springframework.stereotype.Component;
import org.w3c.dom.Attr;

import java.util.List;

import static net.ripe.db.whois.common.rpsl.AttributeType.SPONSORING_ORG;

@Component
public class SponsoringOrgAttributeGenerator extends AttributeGenerator {

    public RpslObject generateAttributes(final RpslObject originalObject, final RpslObject updatedObject, final Update update, final UpdateContext updateContext) {
        switch (updatedObject.getType()) {
            case AUT_NUM:
            case INET6NUM:
            case INETNUM:
                return generateSponsoringOrg(originalObject, updatedObject, update, updateContext);
            default:
                return updatedObject;
        }
    }

    private RpslObject generateSponsoringOrg(final RpslObject originalObject, final RpslObject updatedObject, final Update update, final UpdateContext updateContext) {
        if( hasCorrectNumberOfAttributes(updatedObject,AttributeType.SPONSORING_ORG) == false ) {
            updateContext.addMessage(update, ValidationMessages.tooManyAttributesOfType(AttributeType.SPONSORING_ORG));
        }

        final boolean authByRS = updateContext.getSubject(update).hasPrincipal(Principal.RS_MAINTAINER);
        final boolean isOverride = updateContext.getSubject(update).hasPrincipal(Principal.OVERRIDE_MAINTAINER);

        if (!(authByRS || isOverride) && sponsoringOrgWasRemoved(originalObject, updatedObject)) {
            updateContext.addMessage(update, ValidationMessages.attributeCanBeRemovedOnlyByRipe(AttributeType.SPONSORING_ORG));
            return cleanupAttributeType(update, updateContext, updatedObject, AttributeType.SPONSORING_ORG, originalObject.getValueForAttribute(SPONSORING_ORG).toString());
        }

        return updatedObject;
    }

    private boolean sponsoringOrgWasRemoved(final RpslObject original, final RpslObject updated) {
        return original != null
                && original.containsAttribute(AttributeType.SPONSORING_ORG)
                && !updated.containsAttribute(AttributeType.SPONSORING_ORG);
    }

    private boolean hasCorrectNumberOfAttributes( final RpslObject updatedObject, AttributeType attributeType) {
        boolean status = true;

        final int sponsoringOrgAttributeCount = updatedObject.findAttributes(attributeType).size();
        final AttributeTemplate.Cardinality cardinality = getCardinality(updatedObject.getType(),attributeType);

        if( cardinality == AttributeTemplate.Cardinality.SINGLE && sponsoringOrgAttributeCount > 1 ) {
            status = false;
        }
        return status;
    }

    private AttributeTemplate.Cardinality getCardinality(ObjectType objectType, AttributeType attributeType ) {
        AttributeTemplate.Cardinality cardinality = AttributeTemplate.Cardinality.SINGLE;
        for( AttributeTemplate attributeTemplate : ObjectTemplate.getTemplate(objectType).getAttributeTemplates()) {
            if( attributeTemplate.getAttributeType() == attributeType ) {
                cardinality = attributeTemplate.getCardinality();
                break;
            }
        }
        return  cardinality;
    }


}
