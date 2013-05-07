package net.ripe.db.whois.query.planner;

import com.google.common.base.Function;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;

import javax.annotation.Nullable;
import java.util.*;

class ToBriefFunction implements Function<ResponseObject, ResponseObject> {
    private static final Set<AttributeType> BRIEF_ATTRIBUTES = Sets.immutableEnumSet(AttributeType.INETNUM, AttributeType.INET6NUM, AttributeType.ABUSE_MAILBOX);
    private final AbuseCFinder abuseCFinder;

    public ToBriefFunction(final AbuseCFinder abuseCFinder) {
        this.abuseCFinder = abuseCFinder;
    }

    @Override
    public ResponseObject apply(final @Nullable ResponseObject input) {
        if (!(input instanceof RpslObject)) {
            return input;
        }

        final RpslObject rpslObject = (RpslObject) input;
        final List<RpslAttribute> attributes = rpslObject.getAttributes();
        final List<RpslAttribute> newAttributes = new ArrayList<RpslAttribute>();
        final List<RpslAttribute> abuseCAttributes = new ArrayList<RpslAttribute>();

        if (rpslObject.getType() == ObjectType.INETNUM || rpslObject.getType() == ObjectType.INET6NUM) {
            final Map<CIString, CIString> abuseContacts = abuseCFinder.getAbuseContacts(rpslObject);
            if (!abuseContacts.isEmpty()) {
                abuseCAttributes.add(rpslObject.getTypeAttribute());
                final Iterator<CIString> iterator = abuseContacts.keySet().iterator();
                while (iterator.hasNext()) {
                    final CIString abuseContact = abuseContacts.get(iterator.next());
                    abuseCAttributes.add(new RpslAttribute(AttributeType.ABUSE_MAILBOX, abuseContact.toString()));
                }
                return new RpslAttributes(abuseCAttributes);
            }
        }

        for (final RpslAttribute attribute : attributes) {
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
