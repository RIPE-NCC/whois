package net.ripe.db.whois.common.rpsl;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.WordUtils;

import javax.annotation.Nullable;
import java.util.*;

import static net.ripe.db.whois.common.rpsl.AttributeTemplate.Cardinality.MULTIPLE;
import static net.ripe.db.whois.common.rpsl.AttributeTemplate.Cardinality.SINGLE;
import static net.ripe.db.whois.common.rpsl.AttributeTemplate.Key;
import static net.ripe.db.whois.common.rpsl.AttributeTemplate.Key.*;
import static net.ripe.db.whois.common.rpsl.AttributeTemplate.Requirement.*;
import static net.ripe.db.whois.common.rpsl.AttributeType.*;

public final class ObjectTemplate implements Comparable<ObjectTemplate> {
    private static final Map<ObjectType, ObjectTemplate> TEMPLATE_MAP;

    static {
        final ArrayList<ObjectTemplate> objectTemplates = Lists.newArrayList(

                new ObjectTemplate(ObjectType.AS_BLOCK, 7,
                        new AttributeTemplate(AS_BLOCK, MANDATORY, SINGLE, PRIMARY_KEY, LOOKUP_KEY),
                        new AttributeTemplate(DESCR, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(REMARKS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(ORG, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(NOTIFY, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_BY, MANDATORY, SINGLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_LOWER, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(CHANGED, MANDATORY, MULTIPLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplate(ObjectType.AS_SET, 9,
                        new AttributeTemplate(AS_SET, MANDATORY, SINGLE, PRIMARY_KEY, LOOKUP_KEY),
                        new AttributeTemplate(DESCR, MANDATORY, MULTIPLE),
                        new AttributeTemplate(MEMBERS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(MBRS_BY_REF, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(REMARKS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(ORG, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(TECH_C, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(ADMIN_C, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(NOTIFY, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_BY, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_LOWER, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(CHANGED, MANDATORY, MULTIPLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplate(ObjectType.AUT_NUM, 8,
                        new AttributeTemplate(AUT_NUM, MANDATORY, SINGLE, PRIMARY_KEY, LOOKUP_KEY),
                        new AttributeTemplate(AS_NAME, MANDATORY, SINGLE),
                        new AttributeTemplate(DESCR, MANDATORY, MULTIPLE),
                        new AttributeTemplate(MEMBER_OF, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(IMPORT_VIA, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(IMPORT, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(MP_IMPORT, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(EXPORT_VIA, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(EXPORT, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(MP_EXPORT, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(DEFAULT, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(MP_DEFAULT, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(REMARKS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(ORG, OPTIONAL, SINGLE, INVERSE_KEY),
                        new AttributeTemplate(SPONSORING_ORG, GENERATED, SINGLE),
                        new AttributeTemplate(ADMIN_C, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(TECH_C, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(NOTIFY, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_LOWER, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_ROUTES, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_BY, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(CHANGED, MANDATORY, MULTIPLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplate(ObjectType.DOMAIN, 30,
                        new AttributeTemplate(DOMAIN, MANDATORY, SINGLE, PRIMARY_KEY, LOOKUP_KEY),
                        new AttributeTemplate(DESCR, MANDATORY, MULTIPLE),
                        new AttributeTemplate(ORG, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(ADMIN_C, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(TECH_C, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(ZONE_C, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(NSERVER, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(DS_RDATA, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(REMARKS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(NOTIFY, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_BY, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(CHANGED, MANDATORY, MULTIPLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplate(ObjectType.FILTER_SET, 21,
                        new AttributeTemplate(FILTER_SET, MANDATORY, SINGLE, PRIMARY_KEY, LOOKUP_KEY),
                        new AttributeTemplate(DESCR, MANDATORY, MULTIPLE),
                        new AttributeTemplate(FILTER, OPTIONAL, SINGLE),
                        new AttributeTemplate(MP_FILTER, OPTIONAL, SINGLE),
                        new AttributeTemplate(REMARKS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(ORG, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(TECH_C, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(ADMIN_C, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(NOTIFY, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_BY, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_LOWER, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(CHANGED, MANDATORY, MULTIPLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplate(ObjectType.INET_RTR, 15,
                        new AttributeTemplate(INET_RTR, MANDATORY, SINGLE, PRIMARY_KEY, LOOKUP_KEY),
                        new AttributeTemplate(DESCR, MANDATORY, MULTIPLE),
                        new AttributeTemplate(ALIAS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(LOCAL_AS, MANDATORY, SINGLE, INVERSE_KEY),
                        new AttributeTemplate(IFADDR, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(INTERFACE, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(PEER, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(MP_PEER, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(MEMBER_OF, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(REMARKS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(ORG, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(ADMIN_C, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(TECH_C, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(NOTIFY, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_BY, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(CHANGED, MANDATORY, MULTIPLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplate(ObjectType.INET6NUM, 6,
                        new AttributeTemplate(INET6NUM, MANDATORY, SINGLE, PRIMARY_KEY, LOOKUP_KEY),
                        new AttributeTemplate(NETNAME, MANDATORY, SINGLE, LOOKUP_KEY),
                        new AttributeTemplate(DESCR, MANDATORY, MULTIPLE),
                        new AttributeTemplate(COUNTRY, MANDATORY, MULTIPLE),
                        new AttributeTemplate(GEOLOC, OPTIONAL, SINGLE),
                        new AttributeTemplate(LANGUAGE, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(ORG, OPTIONAL, SINGLE, INVERSE_KEY),
                        new AttributeTemplate(SPONSORING_ORG, GENERATED, SINGLE),
                        new AttributeTemplate(ADMIN_C, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(TECH_C, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(STATUS, MANDATORY, SINGLE),
                        new AttributeTemplate(ASSIGNMENT_SIZE, OPTIONAL, SINGLE),
                        new AttributeTemplate(REMARKS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(NOTIFY, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_BY, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_LOWER, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_ROUTES, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_DOMAINS, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_IRT, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(CHANGED, MANDATORY, MULTIPLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplate(ObjectType.INETNUM, 5,
                        new AttributeTemplate(INETNUM, MANDATORY, SINGLE, PRIMARY_KEY, LOOKUP_KEY),
                        new AttributeTemplate(NETNAME, MANDATORY, SINGLE, LOOKUP_KEY),
                        new AttributeTemplate(DESCR, MANDATORY, MULTIPLE),
                        new AttributeTemplate(COUNTRY, MANDATORY, MULTIPLE),
                        new AttributeTemplate(GEOLOC, OPTIONAL, SINGLE),
                        new AttributeTemplate(LANGUAGE, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(ORG, OPTIONAL, SINGLE, INVERSE_KEY),
                        new AttributeTemplate(SPONSORING_ORG, GENERATED, SINGLE),
                        new AttributeTemplate(ADMIN_C, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(TECH_C, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(STATUS, MANDATORY, SINGLE),
                        new AttributeTemplate(REMARKS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(NOTIFY, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_BY, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_LOWER, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_DOMAINS, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_ROUTES, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_IRT, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(CHANGED, MANDATORY, MULTIPLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplate(ObjectType.IRT, 41,
                        new AttributeTemplate(IRT, MANDATORY, SINGLE, PRIMARY_KEY, LOOKUP_KEY),
                        new AttributeTemplate(ADDRESS, MANDATORY, MULTIPLE),
                        new AttributeTemplate(PHONE, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(FAX_NO, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(E_MAIL, MANDATORY, MULTIPLE, LOOKUP_KEY),
                        new AttributeTemplate(ABUSE_MAILBOX, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(SIGNATURE, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(ENCRYPTION, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(ORG, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(ADMIN_C, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(TECH_C, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(AUTH, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(REMARKS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(IRT_NFY, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(NOTIFY, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_BY, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(CHANGED, MANDATORY, MULTIPLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplate(ObjectType.KEY_CERT, 45,
                        new AttributeTemplate(KEY_CERT, MANDATORY, SINGLE, PRIMARY_KEY, LOOKUP_KEY),
                        new AttributeTemplate(METHOD, GENERATED, SINGLE),
                        new AttributeTemplate(OWNER, GENERATED, MULTIPLE),
                        new AttributeTemplate(FINGERPR, GENERATED, SINGLE, INVERSE_KEY),
                        new AttributeTemplate(CERTIF, MANDATORY, MULTIPLE),
                        new AttributeTemplate(ORG, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(REMARKS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(NOTIFY, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(ADMIN_C, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(TECH_C, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_BY, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(CHANGED, MANDATORY, MULTIPLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplate(ObjectType.MNTNER, 40,
                        new AttributeTemplate(MNTNER, MANDATORY, SINGLE, PRIMARY_KEY, LOOKUP_KEY),
                        new AttributeTemplate(DESCR, MANDATORY, MULTIPLE),
                        new AttributeTemplate(ORG, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(ADMIN_C, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(TECH_C, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(UPD_TO, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_NFY, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(AUTH, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(REMARKS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(NOTIFY, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(ABUSE_MAILBOX, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_BY, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(REFERRAL_BY, MANDATORY, SINGLE),
                        new AttributeTemplate(CHANGED, MANDATORY, MULTIPLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplate(ObjectType.ORGANISATION, 48,
                        new AttributeTemplate(ORGANISATION, MANDATORY, SINGLE, PRIMARY_KEY, LOOKUP_KEY),
                        new AttributeTemplate(ORG_NAME, MANDATORY, SINGLE, LOOKUP_KEY),
                        new AttributeTemplate(ORG_TYPE, MANDATORY, SINGLE),
                        new AttributeTemplate(DESCR, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(REMARKS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(ADDRESS, MANDATORY, MULTIPLE),
                        new AttributeTemplate(PHONE, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(FAX_NO, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(E_MAIL, MANDATORY, MULTIPLE, LOOKUP_KEY),
                        new AttributeTemplate(GEOLOC, OPTIONAL, SINGLE),
                        new AttributeTemplate(LANGUAGE, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(ORG, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(ADMIN_C, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(TECH_C, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(ABUSE_C, OPTIONAL, SINGLE, INVERSE_KEY),
                        new AttributeTemplate(REF_NFY, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_REF, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(NOTIFY, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(ABUSE_MAILBOX, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_BY, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(CHANGED, MANDATORY, MULTIPLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplate(ObjectType.PEERING_SET, 22,
                        new AttributeTemplate(PEERING_SET, MANDATORY, SINGLE, PRIMARY_KEY, LOOKUP_KEY),
                        new AttributeTemplate(DESCR, MANDATORY, MULTIPLE),
                        new AttributeTemplate(PEERING, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(MP_PEERING, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(REMARKS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(ORG, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(TECH_C, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(ADMIN_C, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(NOTIFY, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_BY, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_LOWER, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(CHANGED, MANDATORY, MULTIPLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplate(ObjectType.PERSON, 50,
                        new AttributeTemplate(PERSON, MANDATORY, SINGLE, LOOKUP_KEY),
                        new AttributeTemplate(ADDRESS, MANDATORY, MULTIPLE),
                        new AttributeTemplate(PHONE, MANDATORY, MULTIPLE),
                        new AttributeTemplate(FAX_NO, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(E_MAIL, OPTIONAL, MULTIPLE, LOOKUP_KEY),
                        new AttributeTemplate(ORG, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(NIC_HDL, MANDATORY, SINGLE, PRIMARY_KEY, LOOKUP_KEY),
                        new AttributeTemplate(REMARKS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(NOTIFY, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(ABUSE_MAILBOX, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_BY, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(CHANGED, MANDATORY, MULTIPLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplate(ObjectType.POEM, 37,
                        new AttributeTemplate(POEM, MANDATORY, SINGLE, PRIMARY_KEY, LOOKUP_KEY),
                        new AttributeTemplate(DESCR, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(FORM, MANDATORY, SINGLE, INVERSE_KEY),
                        new AttributeTemplate(TEXT, MANDATORY, MULTIPLE),
                        new AttributeTemplate(AUTHOR, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(REMARKS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(NOTIFY, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_BY, MANDATORY, SINGLE, INVERSE_KEY),
                        new AttributeTemplate(CHANGED, MANDATORY, MULTIPLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplate(ObjectType.POETIC_FORM, 36,
                        new AttributeTemplate(POETIC_FORM, MANDATORY, SINGLE, PRIMARY_KEY, LOOKUP_KEY),
                        new AttributeTemplate(DESCR, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(ADMIN_C, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(REMARKS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(NOTIFY, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_BY, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(CHANGED, MANDATORY, MULTIPLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplate(ObjectType.ROLE, 49,
                        new AttributeTemplate(ROLE, MANDATORY, SINGLE, LOOKUP_KEY),
                        new AttributeTemplate(ADDRESS, MANDATORY, MULTIPLE),
                        new AttributeTemplate(PHONE, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(FAX_NO, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(E_MAIL, MANDATORY, MULTIPLE, LOOKUP_KEY),
                        new AttributeTemplate(ORG, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(ADMIN_C, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(TECH_C, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(NIC_HDL, MANDATORY, SINGLE, PRIMARY_KEY, LOOKUP_KEY),
                        new AttributeTemplate(REMARKS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(NOTIFY, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(ABUSE_MAILBOX, OPTIONAL, SINGLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_BY, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(CHANGED, MANDATORY, MULTIPLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplate(ObjectType.ROUTE_SET, 12,
                        new AttributeTemplate(ROUTE_SET, MANDATORY, SINGLE, PRIMARY_KEY, LOOKUP_KEY),
                        new AttributeTemplate(DESCR, MANDATORY, MULTIPLE),
                        new AttributeTemplate(MEMBERS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(MP_MEMBERS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(MBRS_BY_REF, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(REMARKS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(ORG, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(TECH_C, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(ADMIN_C, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(NOTIFY, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_BY, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_LOWER, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(CHANGED, MANDATORY, MULTIPLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplate(ObjectType.ROUTE, 10,
                        new AttributeTemplate(ROUTE, MANDATORY, SINGLE, PRIMARY_KEY, LOOKUP_KEY),
                        new AttributeTemplate(DESCR, MANDATORY, MULTIPLE),
                        new AttributeTemplate(ORIGIN, MANDATORY, SINGLE, PRIMARY_KEY, INVERSE_KEY),
                        new AttributeTemplate(PINGABLE, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(PING_HDL, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(HOLES, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(ORG, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MEMBER_OF, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(INJECT, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(AGGR_MTD, OPTIONAL, SINGLE),
                        new AttributeTemplate(AGGR_BNDRY, OPTIONAL, SINGLE),
                        new AttributeTemplate(EXPORT_COMPS, OPTIONAL, SINGLE),
                        new AttributeTemplate(COMPONENTS, OPTIONAL, SINGLE),
                        new AttributeTemplate(REMARKS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(NOTIFY, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_LOWER, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_ROUTES, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_BY, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(CHANGED, MANDATORY, MULTIPLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplate(ObjectType.ROUTE6, 11,
                        new AttributeTemplate(ROUTE6, MANDATORY, SINGLE, PRIMARY_KEY, LOOKUP_KEY),
                        new AttributeTemplate(DESCR, MANDATORY, MULTIPLE),
                        new AttributeTemplate(ORIGIN, MANDATORY, SINGLE, PRIMARY_KEY, INVERSE_KEY),
                        new AttributeTemplate(PINGABLE, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(PING_HDL, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(HOLES, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(ORG, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MEMBER_OF, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(INJECT, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(AGGR_MTD, OPTIONAL, SINGLE),
                        new AttributeTemplate(AGGR_BNDRY, OPTIONAL, SINGLE),
                        new AttributeTemplate(EXPORT_COMPS, OPTIONAL, SINGLE),
                        new AttributeTemplate(COMPONENTS, OPTIONAL, SINGLE),
                        new AttributeTemplate(REMARKS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(NOTIFY, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_LOWER, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_ROUTES, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_BY, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(CHANGED, MANDATORY, MULTIPLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplate(ObjectType.RTR_SET, 23,
                        new AttributeTemplate(RTR_SET, MANDATORY, SINGLE, PRIMARY_KEY, LOOKUP_KEY),
                        new AttributeTemplate(DESCR, MANDATORY, MULTIPLE),
                        new AttributeTemplate(MEMBERS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(MP_MEMBERS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(MBRS_BY_REF, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(REMARKS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(ORG, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(TECH_C, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(ADMIN_C, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(NOTIFY, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_BY, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_LOWER, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(CHANGED, MANDATORY, MULTIPLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE))
        );

        final Map<ObjectType, ObjectTemplate> templateMap = Maps.newEnumMap(ObjectType.class);
        for (final ObjectTemplate objectTemplate : objectTemplates) {
            templateMap.put(objectTemplate.getObjectType(), objectTemplate);
        }

        TEMPLATE_MAP = Collections.unmodifiableMap(templateMap);
    }

    private class AttributeTypeComparator implements Comparator<RpslAttribute> {
        private EnumMap<AttributeType, Integer> order = new EnumMap(AttributeType.class);
        public AttributeTypeComparator(final AttributeTemplate... attributeTemplates) {
            for (int i = 0; i < attributeTemplates.length; i++) {
                order.put(attributeTemplates[i].getAttributeType(), i);
            }
        }

        @Override
        public int compare(RpslAttribute o1, RpslAttribute o2) {
            return order.get(o1.getType()) - order.get(o2.getType());
        }
    }

    private final ObjectType objectType;
    private final int orderPosition;
    private final Map<AttributeType, AttributeTemplate> attributeTemplateMap;
    private final List<AttributeTemplate> attributeTemplates;
    private final Set<AttributeType> allAttributeTypes;
    private final Set<AttributeType> keyAttributes;
    private final Set<AttributeType> lookupAttributes;
    private final Set<AttributeType> inverseLookupAttributes;
    private final Set<AttributeType> mandatoryAttributes;
    private final Set<AttributeType> multipleAttributes;
    private final Comparator<RpslAttribute> comparator;

    private ObjectTemplate(final ObjectType objectType, final int orderPosition, final AttributeTemplate... attributeTemplates) {
        this.objectType = objectType;
        this.orderPosition = orderPosition;

        this.attributeTemplates = ImmutableList.copyOf(attributeTemplates);
        this.allAttributeTypes = Collections.unmodifiableSet(Sets.newLinkedHashSet(Iterables.transform(this.attributeTemplates, new Function<AttributeTemplate, AttributeType>() {
            @Nullable
            @Override
            public AttributeType apply(final AttributeTemplate input) {
                return input.getAttributeType();
            }
        })));

        this.attributeTemplateMap = Maps.newEnumMap(AttributeType.class);
        for (final AttributeTemplate attributeTemplate : attributeTemplates) {
            this.attributeTemplateMap.put(attributeTemplate.getAttributeType(), attributeTemplate);
        }

        keyAttributes = getAttributes(attributeTemplates, PRIMARY_KEY);
        lookupAttributes = getAttributes(attributeTemplates, LOOKUP_KEY);
        inverseLookupAttributes = getAttributes(attributeTemplates, INVERSE_KEY);
        mandatoryAttributes = getAttributes(attributeTemplates, MANDATORY);
        multipleAttributes = getAttributes(attributeTemplates, MULTIPLE);

        comparator = new AttributeTypeComparator(attributeTemplates);
    }

    private Set<AttributeType> getAttributes(final AttributeTemplate[] attributeTemplates, final Key key) {
        final Set<AttributeType> attributeTypes = Sets.newLinkedHashSet();
        for (final AttributeTemplate attributeTemplate : attributeTemplates) {
            if (attributeTemplate.getKeys().contains(key)) {
                attributeTypes.add(attributeTemplate.getAttributeType());
            }
        }

        return Collections.unmodifiableSet(attributeTypes);
    }

    private Set<AttributeType> getAttributes(final AttributeTemplate[] attributeTemplates, final AttributeTemplate.Requirement requirement) {
        final Set<AttributeType> attributeTypes = Sets.newLinkedHashSet();
        for (final AttributeTemplate attributeTemplate : attributeTemplates) {
            if (attributeTemplate.getRequirement() == requirement) {
                attributeTypes.add(attributeTemplate.getAttributeType());
            }
        }

        return Collections.unmodifiableSet(attributeTypes);
    }

    private Set<AttributeType> getAttributes(final AttributeTemplate[] attributeTemplates, final AttributeTemplate.Cardinality cardinality) {
        final Set<AttributeType> attributeTypes = Sets.newLinkedHashSet();
        for (final AttributeTemplate attributeTemplate : attributeTemplates) {
            if (attributeTemplate.getCardinality() == cardinality) {
                attributeTypes.add(attributeTemplate.getAttributeType());
            }
        }

        return Collections.unmodifiableSet(attributeTypes);
    }

    public static ObjectTemplate getTemplate(final ObjectType type) {
        final ObjectTemplate objectTemplate = TEMPLATE_MAP.get(type);
        if (objectTemplate == null) {
            throw new IllegalStateException("No template for " + type);
        }

        return objectTemplate;
    }

    public static Collection<ObjectTemplate> getTemplates() {
        return TEMPLATE_MAP.values();
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public List<AttributeTemplate> getAttributeTemplates() {
        return attributeTemplates;
    }

    public Set<AttributeType> getAllAttributes() {
        return allAttributeTypes;
    }

    public Set<AttributeType> getKeyAttributes() {
        return keyAttributes;
    }

    public Set<AttributeType> getLookupAttributes() {
        return lookupAttributes;
    }

    public Set<AttributeType> getMandatoryAttributes() {
        return mandatoryAttributes;
    }

    public Set<AttributeType> getInverseLookupAttributes() {
        return inverseLookupAttributes;
    }

    public Set<AttributeType> getMultipleAttributes() {
        return multipleAttributes;
    }

    public Comparator<RpslAttribute> getAttributeTypeComparator() {
        return comparator;
    }

    public boolean isSet() {
        return ObjectType.getSets().contains(objectType);
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && objectType == ((ObjectTemplate) o).objectType;
    }

    @Override
    public int hashCode() {
        return objectType.hashCode();
    }

    @Override
    public int compareTo(final ObjectTemplate o) {
        return orderPosition - o.orderPosition;
    }

    public ObjectMessages validate(final RpslObject rpslObject) {
        final ObjectMessages objectMessages = new ObjectMessages();
        final ObjectType rpslObjectType = rpslObject.getType();

        final Map<AttributeType, Integer> attributeCount = Maps.newEnumMap(AttributeType.class);
        for (final AttributeTemplate attributeTemplate : attributeTemplates) {
            attributeCount.put(attributeTemplate.getAttributeType(), 0);
        }

        for (final RpslAttribute attribute : rpslObject.getAttributes()) {
            final AttributeType attributeType = attribute.getType();
            if (attributeType == null) {
                objectMessages.addMessage(attribute, ValidationMessages.unknownAttribute(attribute.getKey()));
            } else {
                final AttributeTemplate attributeTemplate = attributeTemplateMap.get(attributeType);
                if (attributeTemplate == null) {
                    objectMessages.addMessage(attribute, ValidationMessages.invalidAttributeForObject(attributeType));
                } else if (!attributeTemplate.getRequirement().equals(GENERATED)) {
                    attribute.validateSyntax(rpslObjectType, objectMessages);
                    attributeCount.put(attributeType, attributeCount.get(attributeType) + 1);
                }
            }
        }

        for (final AttributeTemplate attributeTemplate : attributeTemplates) {
            addValidationMessagesForAttributeTemplate(objectMessages, attributeTemplate, attributeCount);
        }

        return objectMessages;
    }

    public void addValidationMessagesForAttributeTemplate(
                        ObjectMessages objectMessages, AttributeTemplate attributeTemplate,
                        Map<AttributeType, Integer> attributeCount){

        final AttributeType attributeType = attributeTemplate.getAttributeType();
        final int attributeTypeCount = attributeCount.get(attributeType);

        if (attributeTemplate.getRequirement() == MANDATORY && attributeTypeCount == 0) {
            objectMessages.addMessage(ValidationMessages.missingMandatoryAttribute(attributeType));
        }

        if (attributeTemplate.getCardinality() == SINGLE && attributeTypeCount > 1) {
            objectMessages.addMessage(ValidationMessages.tooManyAttributesOfType(attributeType));
        }
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();

        for (final AttributeTemplate attributeTemplate : attributeTemplates) {
            result.append(attributeTemplate).append('\n');
        }

        return result.toString();
    }

    public String toVerboseString() {
        final StringBuilder result = new StringBuilder();

        result.append("The ")
                .append(objectType.getName())
                .append(" class:\n\n")
                .append(ObjectDocumentation.getDocumentation(objectType))
                .append('\n')
                .append(toString())
                .append("\nThe content of the attributes of the ")
                .append(objectType.getName())
                .append(" class are defined below:\n\n");

        for (final AttributeTemplate attributeTemplate : attributeTemplates) {
            final AttributeType attributeType = attributeTemplate.getAttributeType();

            String attributeDescription = attributeType.getDescription(objectType);
            if (attributeDescription.indexOf('\n') == -1) {
                attributeDescription = WordUtils.wrap(attributeDescription, 70);
            }

            if (attributeDescription.endsWith("\n")) {
                attributeDescription = attributeDescription.substring(0, attributeDescription.length() - 1);
            }

            String syntaxDescription = attributeType.getSyntax().getDescription(objectType);
            if (syntaxDescription.endsWith("\n")) {
                syntaxDescription = syntaxDescription.substring(0, syntaxDescription.length() - 1);
            }

            result.append(attributeType.getName())
                    .append("\n\n   ")
                    .append(attributeDescription.replaceAll("\n", "\n   "))
                    .append("\n\n     ")
                    .append(syntaxDescription.replaceAll("\n", "\n     "))
                    .append("\n\n");
        }

        return result.toString();
    }

    public boolean hasAttribute(final AttributeType attributeType) {
        return getAllAttributes().contains(attributeType);
    }
}
