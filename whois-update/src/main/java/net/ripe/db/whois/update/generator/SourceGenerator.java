package net.ripe.db.whois.update.generator;

import com.google.common.collect.ImmutableSet;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static net.ripe.db.whois.common.rpsl.AttributeType.SOURCE;

@Component
public class SourceGenerator extends AttributeGenerator {

    private final AuthoritativeResourceData authoritativeResourceData;

    private final CIString source;
    private final CIString nonAuthSource;

    @Autowired
    public SourceGenerator(final AuthoritativeResourceData authoritativeResourceData,
                           @Value("${whois.source}") final String source,
                           @Value("${whois.nonauth.source}") final String nonAuthSource) {
        this.authoritativeResourceData = authoritativeResourceData;
        this.source = ciString(source);
        this.nonAuthSource = ciString(nonAuthSource);
    }

    @Override
    public RpslObject generateAttributes(RpslObject originalObject, RpslObject updatedObject, Update update, UpdateContext updateContext) {
        switch (updatedObject.getType()) {
            case AUT_NUM:
            case ROUTE:
            case ROUTE6:
                return generateSource(updatedObject, update, updateContext);
            default:
                return updatedObject;
        }
    }

    private RpslObject generateSource(final RpslObject updatedObject, final Update update, final UpdateContext updateContext) {
        boolean rsOrOverride = updateContext.getSubject(update).hasPrincipal(Principal.OVERRIDE_MAINTAINER) || updateContext.getSubject(update).hasPrincipal(Principal.RS_MAINTAINER);

        if (update.getOperation() == Operation.DELETE || rsOrOverride) {
            return updatedObject;
        }

        boolean outOfRegion = !authoritativeResourceData.getAuthoritativeResource(this.source).isMaintainedInRirSpace(updatedObject);
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
