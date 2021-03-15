package net.ripe.db.whois.common.rpsl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.rpsl.transform.FilterChangedFunction;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static net.ripe.db.whois.common.rpsl.AttributeType.ABUSE_MAILBOX;
import static net.ripe.db.whois.common.rpsl.AttributeType.ADDRESS;
import static net.ripe.db.whois.common.rpsl.AttributeType.AUTH;
import static net.ripe.db.whois.common.rpsl.AttributeType.E_MAIL;
import static net.ripe.db.whois.common.rpsl.AttributeType.FAX_NO;
import static net.ripe.db.whois.common.rpsl.AttributeType.IRT_NFY;
import static net.ripe.db.whois.common.rpsl.AttributeType.MNT_NFY;
import static net.ripe.db.whois.common.rpsl.AttributeType.NOTIFY;
import static net.ripe.db.whois.common.rpsl.AttributeType.PERSON;
import static net.ripe.db.whois.common.rpsl.AttributeType.PHONE;
import static net.ripe.db.whois.common.rpsl.AttributeType.REF_NFY;
import static net.ripe.db.whois.common.rpsl.AttributeType.UPD_TO;

@Component
public class DummifierCurrent implements Dummifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(DummifierCurrent.class);

    private static final String PERSON_REPLACEMENT = "Name Removed";
    private static final String FILTERED_APPENDIX = " # Filtered";
    private static final Splitter EMAIL_SPLITTER = Splitter.on('@');
    private static final Splitter SPACE_SPLITTER = Splitter.on(' ');

    private static final Set<AttributeType> EMAIL_ATTRIBUTES = Sets.immutableEnumSet(E_MAIL, NOTIFY, REF_NFY, IRT_NFY, MNT_NFY, UPD_TO);
    private static final Set<AttributeType> PHONE_FAX_ATTRIBUTES = Sets.immutableEnumSet(PHONE, FAX_NO);
    private static final FilterChangedFunction FILTER_CHANGED_FUNCTION = new FilterChangedFunction();

    @Override
    public RpslObject dummify(final int version, final RpslObject rpslObject) {
        final ObjectType objectType = rpslObject.getType();
        Validate.isTrue(isAllowed(version, rpslObject), "The version is not supported by this dummifier", version);

        final List<RpslAttribute> attributes = Lists.newArrayList(rpslObject.getAttributes());

        RpslAttribute lastAddressLine = null;
        int lastAddressLineIndex = 0;

        for (int i = 0; i < attributes.size(); i++) {
            RpslAttribute replacement = attributes.get(i);
            final AttributeType attributeType = replacement.getType();

            try {
                if (!(objectType == ObjectType.ROLE && rpslObject.containsAttribute(ABUSE_MAILBOX))) {
                    replacement = replacePerson(attributeType, replacement);
                    replacement = replaceAuth(attributeType, replacement);
                    replacement = replacePhoneFax(attributeType, replacement);

                    if (attributeType == ADDRESS) {
                        lastAddressLine = replacement;
                        lastAddressLineIndex = i;
                        replacement = new RpslAttribute(ADDRESS, "***");
                    }
                }
                replacement = replaceEmail(attributeType, replacement);

                attributes.set(i, replacement);
            } catch (RuntimeException e) {
                // leaving attribute as it is if dummification failed
                LOGGER.debug("Dummifier failed on [" + attributes.get(i).toString().trim() + "]", e);
            }
        }

        if (lastAddressLine != null) {
            attributes.set(lastAddressLineIndex, lastAddressLine);
        }

        return FILTER_CHANGED_FUNCTION.apply(new RpslObject(rpslObject, attributes));
    }

    private RpslAttribute replacePhoneFax(final AttributeType attributeType, final RpslAttribute attribute) {
        if (PHONE_FAX_ATTRIBUTES.contains(attributeType)) {
            char[] phone = attribute.getCleanValue().toString().toCharArray();

            for (int i = phone.length / 2; i < phone.length; i++) {
                if (!Character.isWhitespace(phone[i])) {
                    phone[i] = '.';
                }
            }

            return new RpslAttribute(attributeType, new String(phone));
        }
        return attribute;
    }

    private RpslAttribute replaceEmail(final AttributeType attributeType, final RpslAttribute attribute) {
        if (EMAIL_ATTRIBUTES.contains(attributeType)) {
            Iterator it = EMAIL_SPLITTER.split(attribute.getCleanValue().toString()).iterator();
            it.next();
            return new RpslAttribute(attributeType, "***@" + it.next());
        }
        return attribute;
    }

    // TODO: [AH] we should relay on a single implementation of Authentication Filter; this method duplicates FilterAuthFunction
    private RpslAttribute replaceAuth(final AttributeType attributeType, final RpslAttribute attribute) {
        if (attributeType != AUTH) {
            return attribute;
        }

        String passwordType = SPACE_SPLITTER.split(attribute.getCleanValue().toUpperCase()).iterator().next();
        if (passwordType.endsWith("-PW") || passwordType.startsWith("SSO")) {     // history table has CRYPT-PW, has to be able to dummify that too!
            return new RpslAttribute(AttributeType.AUTH, passwordType + FILTERED_APPENDIX);
        }

        return attribute;
    }

    private RpslAttribute replacePerson(final AttributeType attributeType, final RpslAttribute attribute) {
        if (attributeType == PERSON) {
            return new RpslAttribute(attributeType, PERSON_REPLACEMENT);
        }
        return attribute;
    }

    @Override
    public boolean isAllowed(final int version, final RpslObject object) {
        return version >= 3;
    }
}

