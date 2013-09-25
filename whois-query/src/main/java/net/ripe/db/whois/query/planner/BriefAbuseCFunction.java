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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.ripe.db.whois.common.rpsl.ObjectType.*;

class BriefAbuseCFunction implements Function<ResponseObject, ResponseObject> {
    private static final Set<AttributeType> BRIEF_ATTRIBUTES = Sets.immutableEnumSet(AttributeType.INETNUM, AttributeType.INET6NUM, AttributeType.ABUSE_MAILBOX);
    private static final Set<ObjectType> ABUSE_CONTACT_OBJECT_TYPES = Sets.immutableEnumSet(INET6NUM, INETNUM, AUT_NUM);
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

        if (ABUSE_CONTACT_OBJECT_TYPES.contains(rpslObject.getType())) {
            final Map<CIString, CIString> abuseContacts = abuseCFinder.getAbuseContacts(rpslObject);
            if (!abuseContacts.isEmpty()) {
                final List<RpslAttribute> abuseCAttributes = new ArrayList<>(2);
                abuseCAttributes.add(rpslObject.getTypeAttribute());
                for (final CIString abuseContact : abuseContacts.keySet()) {
                    abuseCAttributes.add(new RpslAttribute(AttributeType.ABUSE_MAILBOX, abuseContacts.get(abuseContact).toString()));
                }
                return new RpslAttributes(abuseCAttributes);
            }
        }

        // TODO: [AH] make this into a distinct responseobject, so that rest api can also display it
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
