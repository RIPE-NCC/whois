package net.ripe.db.whois.update.generator;

import com.google.common.collect.ImmutableSet;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static net.ripe.db.whois.common.rpsl.AttributeType.SOURCE;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROUTE;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROUTE6;

@Component
public class SourceGenerator extends AttributeGenerator {

    private final AuthoritativeResourceData authoritativeResourceData;

    private final RpslObjectDao rpslObjectDao;
    private final CIString source;
    private final CIString nonAuthSource;

    @Autowired
    public SourceGenerator(final AuthoritativeResourceData authoritativeResourceData,
                           @Value("${whois.source}") final String source,
                           @Value("${whois.nonauth.source}") final String nonAuthSource,
                           final RpslObjectDao rpslObjectDao) {
        this.authoritativeResourceData = authoritativeResourceData;
        this.source = ciString(source);
        this.nonAuthSource = ciString(nonAuthSource);
        this.rpslObjectDao = rpslObjectDao;
    }

    @Override
    public RpslObject generateAttributes(final RpslObject originalObject, final RpslObject updatedObject,
                                         final Update update,
                                         final UpdateContext updateContext) {
        return switch (updatedObject.getType()) {
            case AS_SET -> generateAsSetSource(originalObject, updatedObject, update, updateContext);
            case AUT_NUM, ROUTE, ROUTE6 -> generateSource(updatedObject, update, updateContext);
            default -> updatedObject;
        };
    }

    private RpslObject generateAsSetSource(final RpslObject originalObject, final RpslObject updatedObject,
                                           final Update update, final UpdateContext updateContext) {
        if (update.getOperation() == Operation.DELETE) {
            return updatedObject;
        }
        final String asSetKey = updatedObject.getKey().toString();

        final boolean flatAsSet = !asSetKey.contains(":");
        final CIString asSetSource = updatedObject.getValueForAttribute(AttributeType.SOURCE);

        if (flatAsSet){
            if (originalObject == null){
                return updatedObject;
            }
            final CIString originalAsSetSource = originalObject.getValueForAttribute(AttributeType.SOURCE);
            if (asSetSource.equals(originalAsSetSource)) {
                return updatedObject;
            }
            updateContext.addMessage(update, ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(AttributeType.SOURCE));
            return new RpslObjectBuilder(updatedObject).replaceAttribute(updatedObject.findAttribute(AttributeType.SOURCE),
                    new RpslAttribute(AttributeType.SOURCE, originalAsSetSource)).get();
        }

        final String autnumKey = asSetKey.substring(0, asSetKey.indexOf(":"));
        final RpslObject autnumObject = rpslObjectDao.getByKeyOrNull(ObjectType.AUT_NUM, autnumKey);

        if (autnumObject == null){
            return updatedObject;
        }

        final CIString autnumSource = autnumObject.getValueForAttribute(AttributeType.SOURCE);
        if (asSetSource.equals(autnumSource)){
            return updatedObject;
        }

        updateContext.addMessage(update, UpdateMessages.sourceChanged(asSetSource, autnumSource, autnumKey));

        return new RpslObjectBuilder(updatedObject).replaceAttribute(updatedObject.findAttribute(AttributeType.SOURCE),
                new RpslAttribute(AttributeType.SOURCE, autnumSource)).get();
    }
    private RpslObject generateSource(final RpslObject updatedObject, final Update update, final UpdateContext updateContext) {
        if (update.getOperation() == Operation.DELETE) {
            return updatedObject;
        }

        boolean outOfRegion = updatedObject.getType() == ROUTE || updatedObject.getType() == ROUTE6?
                !authoritativeResourceData.getAuthoritativeResource().isRouteMaintainedInRirSpace(updatedObject) :
                !authoritativeResourceData.getAuthoritativeResource().isMaintainedInRirSpace(updatedObject);

        final CIString source = updatedObject.getValueForAttribute(SOURCE);

        if (outOfRegion && source.equals(this.source)) {
            return cleanupAttributeType(update, updateContext, updatedObject, SOURCE, ImmutableSet.of(this.nonAuthSource.toString()));
        }

        if (!outOfRegion && source.equals(this.nonAuthSource)) {
            return cleanupAttributeType(update, updateContext, updatedObject, SOURCE, ImmutableSet.of(this.source.toString()));
        }

        return updatedObject;
    }

}
