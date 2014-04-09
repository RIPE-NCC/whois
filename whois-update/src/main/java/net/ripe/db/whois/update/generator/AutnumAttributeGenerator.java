package net.ripe.db.whois.update.generator;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.attrs.AutnumStatus;
import net.ripe.db.whois.common.source.IllegalSourceException;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.update.domain.LegacyAutnum;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Component
public class AutnumAttributeGenerator extends AttributeGenerator {

    private static final String STATUS_REMARK =  "For information on \"status:\" attribute read http://www.ripe.net/xxxx/as_status_faq.html";

    private final AuthoritativeResourceData authoritativeResourceData;
    private final SourceContext sourceContext;
    private final LegacyAutnum legacyAutnum;

    @Autowired
    public AutnumAttributeGenerator(final AuthoritativeResourceData authoritativeResourceData, final SourceContext sourceContext, final LegacyAutnum legacyAutnum) {
        this.authoritativeResourceData = authoritativeResourceData;
        this.sourceContext = sourceContext;
        this.legacyAutnum = legacyAutnum;
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
        if (update.getOperation() == Operation.DELETE) {
            return updatedObject;
        }

        if ((originalObject != null) &&
                (originalObject.containsAttribute(AttributeType.STATUS)) &&
                (!updatedObject.containsAttribute(AttributeType.STATUS))) {
            updateContext.addMessage(update, UpdateMessages.statusCannotBeRemoved());
        }

        if (isMaintainedByRir(updatedObject)) {
            if (legacyAutnum.contains(updatedObject.getKey())) {
                return setAutnumStatus(updatedObject, AutnumStatus.LEGACY, update, updateContext);
            } else {
                return setAutnumStatus(updatedObject, AutnumStatus.ASSIGNED, update, updateContext);
            }
        }
        return setAutnumStatus(updatedObject, AutnumStatus.OTHER, update, updateContext);
    }

    private boolean isMaintainedByRir(final RpslObject object) {
        try {
            final AuthoritativeResource authoritativeResource = authoritativeResourceData.getAuthoritativeResource(sourceContext.getCurrentSource().getName());
            return authoritativeResource.isMaintainedByRir(ObjectType.AUT_NUM, object.getKey());
        } catch (IllegalSourceException e) {
            return false;
        }
    }

    private RpslObject setAutnumStatus(final RpslObject object, final AutnumStatus autnumStatus, final Update update, final UpdateContext updateContext) {
        final RpslObjectBuilder builder = new RpslObjectBuilder(object);
        cleanupAttributeType(update, updateContext, builder, AttributeType.STATUS, ciString(autnumStatus.toString()));
        generateRemarks(object, builder, update, updateContext);
        return builder.get();
    }

    private void generateRemarks(final RpslObject object, final RpslObjectBuilder builder, final Update update, final UpdateContext updateContext) {
        final Set<CIString> remarks = object.getValuesForAttribute(AttributeType.REMARKS);
        remarks.add(ciString(STATUS_REMARK));
        cleanupAttributeType(update, updateContext, builder, AttributeType.REMARKS, remarks);
    }
}
