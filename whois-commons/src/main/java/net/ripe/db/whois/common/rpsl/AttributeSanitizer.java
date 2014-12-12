package net.ripe.db.whois.common.rpsl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.rpsl.attrs.Changed;
import net.ripe.db.whois.common.rpsl.attrs.DsRdata;
import net.ripe.db.whois.common.rpsl.attrs.NServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.CheckForNull;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO: [AH] during syntax check/sanitization we parse all attributes into their domain object, we should keep a reference to that instead of reparsing all the time
@Component
public class AttributeSanitizer {
    protected final Logger LOGGER = LoggerFactory.getLogger(AttributeSanitizer.class);

    private static final Splitter LINE_SPLITTER = Splitter.on('\n').trimResults().omitEmptyStrings();

    private final DateTimeProvider dateTimeProvider;
    private final Map<AttributeType, Sanitizer> SANITIZER_MAP;
    private final Set<AttributeType> keyAttributes = Sets.newHashSet();

    @Autowired
    public AttributeSanitizer(DateTimeProvider dateTimeProvider) {
        this.dateTimeProvider = dateTimeProvider;

        SANITIZER_MAP = Maps.newEnumMap(AttributeType.class);
        SANITIZER_MAP.put(AttributeType.DOMAIN, new DomainSanitizer());
        SANITIZER_MAP.put(AttributeType.INETNUM, new InetnumSanitizer());
        SANITIZER_MAP.put(AttributeType.INET6NUM, new Inet6numSanitizer());
        SANITIZER_MAP.put(AttributeType.INET_RTR, new InetrtrSanitizer());
        SANITIZER_MAP.put(AttributeType.NSERVER, new NServerSanitizer());
        SANITIZER_MAP.put(AttributeType.ROUTE, new RouteSanitizer());
        SANITIZER_MAP.put(AttributeType.ROUTE6, new Route6Sanitizer());
        SANITIZER_MAP.put(AttributeType.ALIAS, new AliasSanitizer());
        SANITIZER_MAP.put(AttributeType.CHANGED, new ChangedSanitizer());
        SANITIZER_MAP.put(AttributeType.DS_RDATA, new DsRdataSanitizer());
        SANITIZER_MAP.put(AttributeType.SOURCE, new UppercaseSanitizer());
        SANITIZER_MAP.put(AttributeType.STATUS, new UppercaseSanitizer());

        // add the default sanitizer for keys and primary attributes
        for (ObjectTemplate objectTemplate : ObjectTemplate.getTemplates()) {
            keyAttributes.addAll(objectTemplate.getKeyAttributes());
            keyAttributes.add(objectTemplate.getAttributeTemplates().get(0).getAttributeType());
        }

        final Sanitizer defaultSanitizer = new DefaultSanitizer();
        for (AttributeType attributeType : keyAttributes) {
            if (!SANITIZER_MAP.containsKey(attributeType)) {
                SANITIZER_MAP.put(attributeType, defaultSanitizer);
            }
        }
    }

    private boolean existsInList(final List<RpslAttribute> attributes, final AttributeType attributeType) {
        for (RpslAttribute attr : attributes) {
            if (attr.getType().equals(attributeType)) {
                return true;
            }
        }
        return false;
    }

    private List<RpslAttribute> getKeyRelatedAttributes(final RpslObject originalObject) {
        final List<RpslAttribute> keyRelatedAttributes = Lists.newArrayList();
        keyRelatedAttributes.add(originalObject.getTypeAttribute());

        final Set<AttributeType> keyAttributeTypesForObject = ObjectTemplate.getTemplate(originalObject.getType()).getKeyAttributes();

        for (final RpslAttribute attr : originalObject.getAttributes()) {
            if (keyAttributeTypesForObject.contains(attr.getType())) {
                if (existsInList(keyRelatedAttributes, attr.getType()) == false) {
                    keyRelatedAttributes.add(attr);
                }
            }
        }

        return keyRelatedAttributes;
    }

    private List<RpslAttribute> sanitizeKeyAttributes(final List<RpslAttribute> originalAttributes) {
        final List<RpslAttribute> sanitizedAttributes = Lists.newArrayList();

        for (RpslAttribute orgAttr : originalAttributes) {
            String cleanValue = null;

            final Sanitizer sanitizer = SANITIZER_MAP.get(orgAttr.getType());
            if (sanitizer != null) {
                try {
                    cleanValue = sanitizer.sanitize(orgAttr);
                } catch (IllegalArgumentException ignored) {
                    // no break on syntactically broken objects
                }
            }

            if (cleanValue == null) {
                cleanValue = orgAttr.getValue();
            }
            sanitizedAttributes.add(new RpslAttribute(orgAttr.getKey(), cleanValue));
        }

        return sanitizedAttributes;
    }

    public CIString sanitizeKey(final RpslObject originalObject) {
        final List<RpslAttribute> keyRelatedAttributes = getKeyRelatedAttributes(originalObject);
        return new RpslObject(sanitizeKeyAttributes(keyRelatedAttributes)).getKey();
    }

    public RpslObject sanitize(final RpslObject object, final ObjectMessages objectMessages) {
        final Map<RpslAttribute, RpslAttribute> replacements = Maps.newHashMap();
        for (final RpslAttribute attribute : object.getAttributes()) {
            final AttributeType type = attribute.getType();
            String newValue = null;

            final Sanitizer sanitizer = SANITIZER_MAP.get(type);

            if (sanitizer == null) {
                continue;
            }

            try {
                newValue = sanitizer.sanitize(attribute);
            } catch (IllegalArgumentException ignored) {
                // no break on syntactically broken objects
            }

            if (newValue == null) {
                continue;
            }

            final List<Message> attributeMessages = Lists.newArrayList();
            if (!sanitizer.silent() && !attribute.getCleanValue().toString().equals(newValue)) {
                attributeMessages.add(ValidationMessages.attributeValueConverted(attribute.getCleanValue(), newValue));
            }

            if (keyAttributes.contains(type) && attribute.getValue().indexOf('\n') != -1) {
                attributeMessages.add(ValidationMessages.continuationLinesRemoved());
            }

            if (keyAttributes.contains(type) && attribute.getValue().indexOf('#') != -1) {
                attributeMessages.add(ValidationMessages.remarksReformatted());
            }

            final String replacement = newValue + getCommentReplacement(attribute);
            final RpslAttribute transformed = new RpslAttribute(attribute.getKey(), replacement);
            replacements.put(attribute, transformed);

            for (final Message attributeMessage : attributeMessages) {
                objectMessages.addMessage(transformed, attributeMessage);
            }
        }

        return new RpslObjectBuilder(object).replaceAttributes(replacements).get();
    }

    private String getCommentReplacement(final RpslAttribute attribute) {
        final StringBuilder commentBuilder = new StringBuilder();

        for (final String line : LINE_SPLITTER.split(attribute.getValue())) {
            final int commentIdx = line.indexOf('#');
            if (commentIdx != -1 && commentIdx != line.length() - 1) {
                final String comment = line.substring(commentIdx + 1).trim();
                if (!comment.isEmpty()) {
                    if (commentBuilder.length() == 0) {
                        commentBuilder.append(" #");
                    }
                }

                commentBuilder.append(' ');
                commentBuilder.append(comment);
            }
        }

        return commentBuilder.toString();
    }

    private abstract class Sanitizer {
        @CheckForNull
        abstract String sanitize(RpslAttribute attribute);

        boolean silent() {
            return false;
        }
    }

    private class DefaultSanitizer extends Sanitizer {
        @Override
        public String sanitize(final RpslAttribute attribute) {
            return attribute.getCleanValue().toString();
        }
    }

    private class AliasSanitizer extends Sanitizer {
        @Override
        public String sanitize(final RpslAttribute attribute) {
            final String alias = attribute.getCleanValue().toString();
            if (alias.endsWith(".")) {
                return alias.substring(0, alias.length() - 1);
            }

            return null;
        }
    }

    private class ChangedSanitizer extends Sanitizer {
        @Override
        public String sanitize(final RpslAttribute attribute) {
            final Changed changed = Changed.parse(attribute.getCleanValue());
            if (changed.getDate() == null) {
                return new Changed(changed.getEmail(), dateTimeProvider.getCurrentDate()).toString();
            }
            return null;
        }

        @Override
        // this is expected behavior, don't spam users
        public boolean silent() {
            return true;
        }
    }

    private class InetrtrSanitizer extends Sanitizer {
        @Override
        public String sanitize(final RpslAttribute attribute) {
            final String inetRtr = attribute.getCleanValue().toString();
            if (inetRtr.endsWith(".")) {
                return inetRtr.substring(0, inetRtr.length() - 1);
            }

            return null;
        }
    }

    private class DomainSanitizer extends Sanitizer {
        @Override
        public String sanitize(final RpslAttribute attribute) {
            final String domain = attribute.getCleanValue().toString();
            if (domain.endsWith(".")) {
                return domain.substring(0, domain.length() - 1);
            }

            return null;
        }
    }

    private class DsRdataSanitizer extends Sanitizer {
        @Override
        public String sanitize(final RpslAttribute attribute) {
            final DsRdata dsRdata = DsRdata.parse(attribute.getCleanValue().toString());
            return dsRdata.toString();
        }
    }

    private class InetnumSanitizer extends Sanitizer {
        @Override
        public String sanitize(final RpslAttribute attribute) {
            return Ipv4Resource.parse(attribute.getCleanValue()).toRangeString();
        }
    }

    private class Inet6numSanitizer extends Sanitizer {
        @Override
        public String sanitize(final RpslAttribute attribute) {
            return Ipv6Resource.parse(attribute.getCleanValue()).toString();
        }
    }

    private class NServerSanitizer extends Sanitizer {
        @Override
        public String sanitize(final RpslAttribute attribute) {
            return NServer.parse(attribute.getCleanValue()).toString();
        }
    }

    private class RouteSanitizer extends Sanitizer {
        @Override
        public String sanitize(final RpslAttribute attribute) {
            return Ipv4Resource.parse(attribute.getCleanValue()).toString();
        }
    }

    private class Route6Sanitizer extends Sanitizer {
        @Override
        public String sanitize(final RpslAttribute attribute) {
            return Ipv6Resource.parse(attribute.getCleanValue()).toString();
        }
    }

    private class UppercaseSanitizer extends Sanitizer {
        @Override
        public String sanitize(final RpslAttribute attribute) {
            return attribute.getCleanValue().toUpperCase();
        }
    }
}
