package net.ripe.db.whois.common.rpsl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import org.apache.commons.lang.Validate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static net.ripe.db.whois.common.rpsl.AttributeType.*;

@Component
public class DummifierCurrent implements Dummifier {
    private static final String EMAIL_AT = "@";
    private static final String STAR_REPLACEMENT = "* * *";
    private static final String PHONEFAX_REPLACEMENT = ".. ....";
    private static final String PERSON_REPLACEMENT = "Name Removed";
    private static final String MD5_REPLACEMENT = "MD5-PW $1$SaltSalt$DummifiedMD5HashValue.   # Real value hidden for security";

    private static final Set<AttributeType> EMAIL_ATTRIBUTES = Sets.immutableEnumSet(E_MAIL, NOTIFY, CHANGED, REF_NFY, IRT_NFY, MNT_NFY, UPD_TO);
    private static final Set<AttributeType> PHONE_FAX_ATTRIBUTES = Sets.immutableEnumSet(PHONE, FAX_NO);


    public RpslObject dummify(final int version, final RpslObject rpslObject) {
        final ObjectType objectType = rpslObject.getType();
        Validate.isTrue(isAllowed(version, rpslObject), "The version is not supported by this dummifier", version);

        final List<RpslAttribute> attributes = Lists.newArrayList(rpslObject.getAttributes());

        dummify(attributes, (objectType == ObjectType.ROLE && rpslObject.containsAttribute(ABUSE_MAILBOX)));

        return new RpslObject(rpslObject.getObjectId(), attributes);
    }

    private void dummify(final List<RpslAttribute> attributes, final boolean isRoleWithAbuseMailbox) {
        RpslAttribute lastAddressLine = null;
        int lastAddressLineIndex = 0;

        for (int i = 0; i < attributes.size(); i++) {
            RpslAttribute replacement = attributes.get(i);
            final AttributeType attributeType = replacement.getType();

            if (!isRoleWithAbuseMailbox) {
                replacement = replacePerson(attributeType, replacement);
                replacement = replaceAuth(attributeType, replacement);
                replacement = replacePhoneFax(attributeType, replacement);

                if (attributeType == ADDRESS) {
                    lastAddressLine = replacement;
                    lastAddressLineIndex = i;
                    replacement = new RpslAttribute(ADDRESS, STAR_REPLACEMENT);
                }
            }
            replacement = replaceEmail(attributeType, replacement);

            attributes.set(i, replacement);
        }

        if (lastAddressLine != null) {
            attributes.set(lastAddressLineIndex, lastAddressLine);
        }
    }

    private RpslAttribute replacePhoneFax(final AttributeType attributeType, final RpslAttribute attribute) {
        if (PHONE_FAX_ATTRIBUTES.contains(attributeType)) {
            int length = attribute.getCleanValue().toString().length();
            if (length % 2 != 0) {
                length += 1;
            }
            return new RpslAttribute(attributeType, attribute.getCleanValue().subSequence(0, (length / 2)).toString() + PHONEFAX_REPLACEMENT);
        }
        return attribute;
    }

    private RpslAttribute replaceEmail(final AttributeType attributeType, final RpslAttribute attribute) {
        if (EMAIL_ATTRIBUTES.contains(attributeType)) {
            final String[] email = attribute.getCleanValue().toString().split(EMAIL_AT);
            return new RpslAttribute(attributeType, STAR_REPLACEMENT + EMAIL_AT + email[1]);
        }
        return attribute;
    }

    private RpslAttribute replaceAuth(final AttributeType attributeType, final RpslAttribute attribute) {
        if (attributeType == AUTH && attribute.getCleanValue().contains(CIString.ciString("MD5"))) {
            return new RpslAttribute(attributeType, MD5_REPLACEMENT);
        }
        return attribute;
    }

    private RpslAttribute replacePerson(final AttributeType attributeType, final RpslAttribute attribute) {
        if (attributeType == PERSON) {
            return new RpslAttribute(attributeType, PERSON_REPLACEMENT);
        }
        return attribute;
    }

    public boolean isAllowed(final int version, final RpslObject object) {
        return version >= 3;
    }
}

