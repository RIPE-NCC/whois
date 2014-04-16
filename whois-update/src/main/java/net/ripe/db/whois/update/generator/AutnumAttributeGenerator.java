package net.ripe.db.whois.update.generator;

import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
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

import java.util.List;

@Component
public class AutnumAttributeGenerator extends AttributeGenerator {

    static final String REMARKS_TEXT = "For information on \"status:\" attribute read https://www.ripe.net/data-tools/db/faq/faq-status-values-legacy-resources";
    static final RpslAttribute STATUS_REMARK = new RpslAttribute(AttributeType.REMARKS, REMARKS_TEXT);

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
        cleanupAttributeType(update, updateContext, builder, AttributeType.STATUS, autnumStatus.toString());
        enforceRemarksRightBeforeStatus(builder);
        return builder.get();
    }

    private void enforceRemarksRightBeforeStatus(final RpslObjectBuilder builder) {
        boolean found = false;
        final List<RpslAttribute> attributes = builder.getAttributes();

        for (int i = 0; i < attributes.size(); i++) {
            if (attributes.get(i).equals(STATUS_REMARK)) {
                if (i + 1 < attributes.size() && attributes.get(i + 1).getType().equals(AttributeType.STATUS)) {
                    found = true;
                } else {
                    attributes.remove(i--);
                }
            }
        }

        if (found) return;

        for (int i = 0; i < attributes.size(); i++) {
            if (attributes.get(i).getType() == AttributeType.STATUS) {
                builder.addAttribute(i, STATUS_REMARK);
                break;
            }
        }
    }
}
