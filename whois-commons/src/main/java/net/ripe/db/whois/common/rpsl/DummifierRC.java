package net.ripe.db.whois.common.rpsl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import net.ripe.db.whois.common.rpsl.transform.FilterChangedFunction;
import net.ripe.db.whois.common.Validate;
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
public class DummifierRC implements Dummifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(DummifierRC.class);

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

            replacement = dummifyOrgName(rpslObject, replacement);
            replacement = dummifyDescr(replacement);
            replacement = dummifyRemarks(replacement);
            replacement = dummifyOwner(replacement);

            attributes.set(i, replacement);

            try {
                if (!(objectType == ObjectType.ROLE && rpslObject.containsAttribute(ABUSE_MAILBOX))) {
                    replacement = replacePerson(replacement);
                    replacement = replaceAuth(replacement);
                    replacement = replacePhoneFax(replacement);

                    if (replacement.getType() == ADDRESS) {
                        lastAddressLine = replacement;
                        lastAddressLineIndex = i;
                        replacement = new RpslAttribute(ADDRESS, "***");
                    }
                }
                replacement = replaceEmail(replacement);

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

    private RpslAttribute dummifyOrgName(final RpslObject rpslObject, final RpslAttribute rpslAttribute) {
        if (rpslAttribute.getType() != AttributeType.ORG_NAME) {
            return rpslAttribute;
        }

        return new RpslAttribute(AttributeType.ORG_NAME, new PhoneticDummifier(rpslObject.getValueForAttribute(AttributeType.ORG_NAME).toString()).toString());
    }

    private RpslAttribute dummifyDescr(final RpslAttribute rpslAttribute) {
        if (rpslAttribute.getType() != AttributeType.DESCR) {
            return rpslAttribute;
        }

        return new RpslAttribute(AttributeType.DESCR, "***");
    }

    private RpslAttribute dummifyRemarks(final RpslAttribute rpslAttribute) {
        if (rpslAttribute.getType() != AttributeType.REMARKS) {
            return rpslAttribute;
        }

        return new RpslAttribute(AttributeType.REMARKS, "***");
    }

    private RpslAttribute dummifyOwner(final RpslAttribute rpslAttribute) {
        if (rpslAttribute.getType() != AttributeType.OWNER) {
            return rpslAttribute;
        }

        return new RpslAttribute(AttributeType.OWNER, "***");
    }

    private RpslAttribute replacePhoneFax(final RpslAttribute attribute) {
        if (PHONE_FAX_ATTRIBUTES.contains(attribute.getType())) {
            char[] phone = attribute.getCleanValue().toString().toCharArray();

            for (int i = phone.length / 2; i < phone.length; i++) {
                if (!Character.isWhitespace(phone[i])) {
                    phone[i] = '.';
                }
            }

            return new RpslAttribute(attribute.getType(), new String(phone));
        }
        return attribute;
    }

    private RpslAttribute replaceEmail(final RpslAttribute attribute) {
        if (EMAIL_ATTRIBUTES.contains(attribute.getType())) {
            final InternetAddress internetAddress;
            try {
                internetAddress = new InternetAddress(attribute.getCleanValue().toString(), false);
            } catch (final AddressException e) {
                LOGGER.debug("{} is an invalid email address", attribute.getCleanValue());
                return new RpslAttribute(attribute.getType(), "***");
            }

            final Iterator it = EMAIL_SPLITTER.split(internetAddress.getAddress()).iterator();
            it.next();
            return new RpslAttribute(attribute.getType(), "***@" + it.next());
        }
        return attribute;
    }

    // TODO: [AH] we should relay on a single implementation of Authentication Filter; this method duplicates FilterAuthFunction
    private RpslAttribute replaceAuth(final RpslAttribute attribute) {
        if (attribute.getType() != AUTH) {
            return attribute;
        }

        String passwordType = SPACE_SPLITTER.split(attribute.getCleanValue().toUpperCase()).iterator().next();
        if (passwordType.endsWith("-PW") || passwordType.startsWith("SSO")) {     // history table has CRYPT-PW, has to be able to dummify that too!
            return new RpslAttribute(AttributeType.AUTH, passwordType + FILTERED_APPENDIX);
        }

        return attribute;
    }

    private RpslAttribute replacePerson(final RpslAttribute attribute) {
        if (attribute.getType() == PERSON) {
            return new RpslAttribute(attribute.getType(), PERSON_REPLACEMENT);
        }
        return attribute;
    }

    @Override
    public boolean isAllowed(final int version, final RpslObject object) {
        return version >= 3;
    }
}
