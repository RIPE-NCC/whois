package net.ripe.db.whois.common.rpsl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.domain.Ipv6Resource;
import net.ripe.db.whois.common.domain.attrs.Changed;
import net.ripe.db.whois.common.domain.attrs.NServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.CheckForNull;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class AttributeSanitizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AttributeSanitizer.class);
    private static final Splitter LINE_SPLITTER = Splitter.on('\n').trimResults().omitEmptyStrings();

    private final DateTimeProvider dateTimeProvider;
    private final Map<AttributeType, Sanitizer> SANITIZER_MAP;

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

        // add the default sanitizer for keys and primary attributes
        final Set<AttributeType> alwaysSanitize = Sets.newHashSet();
        for (ObjectTemplate objectTemplate : ObjectTemplate.getTemplates()) {
            alwaysSanitize.addAll(objectTemplate.getKeyAttributes());
            alwaysSanitize.add(objectTemplate.getAttributeTemplates().get(0).getAttributeType());
        }

        Sanitizer defaultSanitizer = new DefaultSanitizer();
        for (AttributeType attributeType : alwaysSanitize) {
            if (!SANITIZER_MAP.containsKey(attributeType)) {
                SANITIZER_MAP.put(attributeType, defaultSanitizer);
            }
        }
    }

    public RpslObject sanitize(final RpslObject object, final ObjectMessages objectMessages) {
        final Map<RpslAttribute, RpslAttribute> replacements = Maps.newHashMap();
        for (final RpslAttribute attribute : object.getAttributes()) {
            final AttributeType type = attribute.getType();
            String newValue = null;

            Sanitizer sanitizer = SANITIZER_MAP.get(type);

            if (sanitizer == null) {
                continue;
            }

            try {
                newValue = sanitizer.sanitize(object, attribute);
            } catch (IllegalArgumentException ignored) {} // no break on syntactically broken objects

            if (newValue == null) {
                continue;
            }

            final List<Message> attributeMessages = Lists.newArrayList();
            if (!sanitizer.silent() && !attribute.getCleanValue().toString().equals(newValue)) {
                attributeMessages.add(ValidationMessages.attributeValueConverted(attribute.getCleanValue(), newValue));
            }

            if (attribute.getValue().indexOf('\n') != -1) {
                attributeMessages.add(ValidationMessages.continuationLinesRemoved());
            }

            if (attribute.getValue().indexOf('#') != -1) {
                attributeMessages.add(ValidationMessages.remarksReformatted());
            }

            final String replacement = newValue + getCommentReplacement(attribute);
            final RpslAttribute transformed = new RpslAttribute(attribute.getKey(), replacement);
            replacements.put(attribute, transformed);

            for (final Message attributeMessage : attributeMessages) {
                objectMessages.addMessage(transformed, attributeMessage);
            }
        }

        return new RpslObjectFilter(object).replaceAttributes(replacements);
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
        abstract String sanitize(RpslObject object, RpslAttribute attribute);

        boolean silent() {
            return false;
        }
    }

    private class DefaultSanitizer extends Sanitizer {
        @Override
        public String sanitize(final RpslObject object, final RpslAttribute attribute) {
            return attribute.getCleanValue().toString();
        }
    }

    private class AliasSanitizer extends Sanitizer {
        @Override
        public String sanitize(final RpslObject object, final RpslAttribute attribute) {
            final String alias = attribute.getCleanValue().toString();
            if (alias.endsWith(".")) {
                return alias.substring(0, alias.length() - 1);
            }

            return null;
        }
    }

    private class ChangedSanitizer extends Sanitizer {
        @Override
        public String sanitize(final RpslObject object, final RpslAttribute attribute) {
            final Changed changed = Changed.parse(attribute.getCleanValue());
            if (changed.getDate() == null) {
                return new Changed(changed.getEmail(), dateTimeProvider.getCurrentDate()).toString();
            }
            return null;
        }

        @Override
        public boolean silent() {
            return true;
        }
    }

    private class InetrtrSanitizer extends Sanitizer {
        @Override
        public String sanitize(final RpslObject object, final RpslAttribute attribute) {
            final String inet_rtr = attribute.getCleanValue().toString();
            if (inet_rtr.endsWith(".")) {
                return inet_rtr.substring(0, inet_rtr.length() - 1);
            }

            return null;
        }
    }

    private class DomainSanitizer extends Sanitizer {
        @Override
        public String sanitize(final RpslObject object, final RpslAttribute attribute) {
            final String domain = attribute.getCleanValue().toString();
            if (domain.endsWith(".")) {
                return domain.substring(0, domain.length() - 1);
            }

            return null;
        }

    }

    private class InetnumSanitizer extends Sanitizer {
        @Override
        public String sanitize(final RpslObject object, final RpslAttribute attribute) {
            return Ipv4Resource.parse(attribute.getCleanValue()).toRangeString();
        }
    }

    private class Inet6numSanitizer extends Sanitizer {
        @Override
        public String sanitize(final RpslObject object, final RpslAttribute attribute) {
            return Ipv6Resource.parse(attribute.getCleanValue()).toString();
        }
    }

    private class NServerSanitizer extends Sanitizer {
        @Override
        public String sanitize(final RpslObject object, final RpslAttribute attribute) {
            return NServer.parse(attribute.getCleanValue()).toString();
        }
    }

    private class RouteSanitizer extends Sanitizer {
        @Override
        public String sanitize(final RpslObject object, final RpslAttribute attribute) {
            return Ipv4Resource.parse(attribute.getCleanValue()).toString();
        }
    }

    private class Route6Sanitizer extends Sanitizer {
        @Override
        public String sanitize(final RpslObject object, final RpslAttribute attribute) {
            return Ipv6Resource.parse(attribute.getCleanValue()).toString();
        }
    }
}
