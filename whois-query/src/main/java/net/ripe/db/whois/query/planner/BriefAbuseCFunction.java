package net.ripe.db.whois.query.planner;

import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.query.Query;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@ThreadSafe
class BriefAbuseCFunction implements Function<ResponseObject, ResponseObject> {
    private static final EnumSet<AttributeType> BRIEF_ATTRIBUTES = EnumSet.of(AttributeType.INETNUM, AttributeType.INET6NUM, AttributeType.AUT_NUM, AttributeType.ABUSE_MAILBOX);
    private final AbuseCFinder abuseCFinder;

    public BriefAbuseCFunction(final AbuseCFinder abuseCFinder) {
        this.abuseCFinder = abuseCFinder;
    }

    @Override
    public ResponseObject apply(final @Nullable ResponseObject input) {
        if (!(input instanceof RpslObject)) {
            return input;
        }

        final RpslObject rpslObject = (RpslObject) input;

        // related IRT object could still be in the resultset with -b
        if (Query.ABUSE_CONTACT_OBJECT_TYPES.contains(rpslObject.getType())) {
            final Optional<AbuseContact> abuseContact = abuseCFinder.getAbuseContact(rpslObject);
            if (abuseContact.isPresent()) {
                final List<RpslAttribute> abuseCAttributes = new ArrayList<>(2);
                abuseCAttributes.add(rpslObject.getTypeAttribute());
                abuseCAttributes.add(new RpslAttribute(AttributeType.ABUSE_MAILBOX, abuseContact.get().getAbuseMailbox()));
                return new RpslAttributes(abuseCAttributes);
            }
        }

        final List<RpslAttribute> newAttributes = new ArrayList<>(2);
        for (final RpslAttribute attribute : rpslObject.getAttributes()) {
            if (BRIEF_ATTRIBUTES.contains(attribute.getType())) {
                newAttributes.add(attribute);
            }
        }

        if (newAttributes.isEmpty()) {
            return null;
        }

        return new RpslAttributes(newAttributes);
    }
}
