package net.ripe.db.whois.common.rpsl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.domain.Ipv6Resource;
import net.ripe.db.whois.common.domain.attrs.NServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.CheckForNull;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class AttributeSanitizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AttributeSanitizer.class);
    private static final Splitter LINE_SPLITTER = Splitter.on('\n').trimResults().omitEmptyStrings();
    private static final Map<AttributeType, Sanitizer> SANITIZER_MAP;

    static {
        SANITIZER_MAP = Maps.newEnumMap(AttributeType.class);
        SANITIZER_MAP.put(AttributeType.DOMAIN, new DomainSanitizer());
        SANITIZER_MAP.put(AttributeType.INETNUM, new InetnumSanitizer());
        SANITIZER_MAP.put(AttributeType.INET6NUM, new Inet6numSanitizer());
        SANITIZER_MAP.put(AttributeType.INET_RTR, new InetrtrSanitizer());
        SANITIZER_MAP.put(AttributeType.NSERVER, new NServerSanitizer());
        SANITIZER_MAP.put(AttributeType.ROUTE, new RouteSanitizer());
        SANITIZER_MAP.put(AttributeType.ROUTE6, new Route6Sanitizer());
        SANITIZER_MAP.put(AttributeType.ALIAS, new AliasSanitizer());
    }

    public RpslObject sanitize(final RpslObject object, final ObjectMessages objectMessages) {
        final ObjectTemplate objectTemplate = ObjectTemplate.getTemplate(object.getType());

        final Set<AttributeType> alwaysSanitize = Sets.newLinkedHashSet();
        alwaysSanitize.addAll(objectTemplate.getKeyAttributes());
        alwaysSanitize.add(objectTemplate.getAttributeTemplates().get(0).getAttributeType());

        final Map<RpslAttribute, RpslAttribute> replacements = Maps.newHashMap();
        for (final RpslAttribute attribute : object.getAttributes()) {
            final String valueReplacement = getValueReplacement(alwaysSanitize, object, attribute);
            if (valueReplacement == null) {
                continue;
            }

            final List<Message> attributeMessages = Lists.newArrayList();
            if (!attribute.getCleanValue().toString().equals(valueReplacement)) {
                attributeMessages.add(ValidationMessages.attributeValueConverted(attribute.getCleanValue(), valueReplacement));
            }

            if (attribute.getValue().indexOf('\n') != -1) {
                attributeMessages.add(ValidationMessages.continuationLinesRemoved());
            }

            if (attribute.getValue().indexOf('#') != -1) {
                attributeMessages.add(ValidationMessages.remarksReformatted());
            }

            final String replacement = valueReplacement + getCommentReplacement(attribute);
            final RpslAttribute transformed = new RpslAttribute(attribute.getKey(), replacement);
            replacements.put(attribute, transformed);

            for (final Message attributeMessage : attributeMessages) {
                objectMessages.addMessage(transformed, attributeMessage);
            }
        }

        return new RpslObjectFilter(object).replaceAttributes(replacements);
    }

    @CheckForNull
    private String getValueReplacement(final Set<AttributeType> alwaysSanitize, final RpslObject object, final RpslAttribute attribute) {
        final Sanitizer sanitizer = SANITIZER_MAP.get(attribute.getType());

        if (sanitizer != null) {
            try {
                return sanitizer.sanitize(object, attribute);
            } catch (IllegalArgumentException ignored) {
                LOGGER.debug("Do nothing, error will be reported by syntax checker");
            }
        }

        if (alwaysSanitize.contains(attribute.getType())) {
            return attribute.getCleanValue().toString();
        }

        return null;
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

    private interface Sanitizer {
        @CheckForNull
        String sanitize(RpslObject object, RpslAttribute attribute);
    }

    private static class AliasSanitizer implements Sanitizer {
        @Override
        public String sanitize(final RpslObject object, final RpslAttribute attribute) {
            final String alias = attribute.getCleanValue().toString();
            if (alias.endsWith(".")) {
                return alias.substring(0, alias.length() - 1);
            }

            return null;
        }
    }

    private static class InetrtrSanitizer implements Sanitizer {
        @Override
        public String sanitize(final RpslObject object, final RpslAttribute attribute) {
            final String inet_rtr = attribute.getCleanValue().toString();
            if (inet_rtr.endsWith(".")) {
                return inet_rtr.substring(0, inet_rtr.length() - 1);
            }

            return null;
        }
    }

    private static class DomainSanitizer implements Sanitizer {
        @Override
        public String sanitize(final RpslObject object, final RpslAttribute attribute) {
            final String domain = attribute.getCleanValue().toString();
            if (domain.endsWith(".")) {
                return domain.substring(0, domain.length() - 1);
            }

            return null;
        }
    }

    private static class InetnumSanitizer implements Sanitizer {
        @Override
        public String sanitize(final RpslObject object, final RpslAttribute attribute) {
            return Ipv4Resource.parse(attribute.getCleanValue()).toRangeString();
        }
    }

    private static class Inet6numSanitizer implements Sanitizer {
        @Override
        public String sanitize(final RpslObject object, final RpslAttribute attribute) {
            return Ipv6Resource.parse(attribute.getCleanValue()).toString();
        }
    }

    private static class NServerSanitizer implements Sanitizer {
        @Override
        public String sanitize(final RpslObject object, final RpslAttribute attribute) {
            return NServer.parse(attribute.getCleanValue()).toString();
        }
    }

    private static class RouteSanitizer implements Sanitizer {
        @Override
        public String sanitize(final RpslObject object, final RpslAttribute attribute) {
            return Ipv4Resource.parse(attribute.getCleanValue()).toString();
        }
    }

    private static class Route6Sanitizer implements Sanitizer {
        @Override
        public String sanitize(final RpslObject object, final RpslAttribute attribute) {
            return Ipv6Resource.parse(attribute.getCleanValue()).toString();
        }
    }
}
