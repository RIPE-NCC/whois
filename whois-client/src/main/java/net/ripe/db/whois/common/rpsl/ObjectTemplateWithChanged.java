package net.ripe.db.whois.common.rpsl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static net.ripe.db.whois.common.rpsl.AttributeTemplate.Cardinality.MULTIPLE;
import static net.ripe.db.whois.common.rpsl.AttributeTemplate.Cardinality.SINGLE;
import static net.ripe.db.whois.common.rpsl.AttributeTemplate.Key.INVERSE_KEY;
import static net.ripe.db.whois.common.rpsl.AttributeTemplate.Key.LOOKUP_KEY;
import static net.ripe.db.whois.common.rpsl.AttributeTemplate.Key.PRIMARY_KEY;
import static net.ripe.db.whois.common.rpsl.AttributeTemplate.Order.USER_ORDER;
import static net.ripe.db.whois.common.rpsl.AttributeTemplate.Requirement.DEPRECATED;
import static net.ripe.db.whois.common.rpsl.AttributeTemplate.Requirement.GENERATED;
import static net.ripe.db.whois.common.rpsl.AttributeTemplate.Requirement.MANDATORY;
import static net.ripe.db.whois.common.rpsl.AttributeTemplate.Requirement.OPTIONAL;
import static net.ripe.db.whois.common.rpsl.AttributeType.ABUSE_C;
import static net.ripe.db.whois.common.rpsl.AttributeType.ABUSE_MAILBOX;
import static net.ripe.db.whois.common.rpsl.AttributeType.ADDRESS;
import static net.ripe.db.whois.common.rpsl.AttributeType.ADMIN_C;
import static net.ripe.db.whois.common.rpsl.AttributeType.AGGR_BNDRY;
import static net.ripe.db.whois.common.rpsl.AttributeType.AGGR_MTD;
import static net.ripe.db.whois.common.rpsl.AttributeType.ALIAS;
import static net.ripe.db.whois.common.rpsl.AttributeType.ASSIGNMENT_SIZE;
import static net.ripe.db.whois.common.rpsl.AttributeType.AS_BLOCK;
import static net.ripe.db.whois.common.rpsl.AttributeType.AS_NAME;
import static net.ripe.db.whois.common.rpsl.AttributeType.AS_SET;
import static net.ripe.db.whois.common.rpsl.AttributeType.AUTH;
import static net.ripe.db.whois.common.rpsl.AttributeType.AUTHOR;
import static net.ripe.db.whois.common.rpsl.AttributeType.AUT_NUM;
import static net.ripe.db.whois.common.rpsl.AttributeType.CERTIF;
import static net.ripe.db.whois.common.rpsl.AttributeType.CHANGED;
import static net.ripe.db.whois.common.rpsl.AttributeType.COMPONENTS;
import static net.ripe.db.whois.common.rpsl.AttributeType.COUNTRY;
import static net.ripe.db.whois.common.rpsl.AttributeType.CREATED;
import static net.ripe.db.whois.common.rpsl.AttributeType.DEFAULT;
import static net.ripe.db.whois.common.rpsl.AttributeType.DESCR;
import static net.ripe.db.whois.common.rpsl.AttributeType.DOMAIN;
import static net.ripe.db.whois.common.rpsl.AttributeType.DS_RDATA;
import static net.ripe.db.whois.common.rpsl.AttributeType.ENCRYPTION;
import static net.ripe.db.whois.common.rpsl.AttributeType.EXPORT;
import static net.ripe.db.whois.common.rpsl.AttributeType.EXPORT_COMPS;
import static net.ripe.db.whois.common.rpsl.AttributeType.EXPORT_VIA;
import static net.ripe.db.whois.common.rpsl.AttributeType.E_MAIL;
import static net.ripe.db.whois.common.rpsl.AttributeType.FAX_NO;
import static net.ripe.db.whois.common.rpsl.AttributeType.FILTER;
import static net.ripe.db.whois.common.rpsl.AttributeType.FILTER_SET;
import static net.ripe.db.whois.common.rpsl.AttributeType.FINGERPR;
import static net.ripe.db.whois.common.rpsl.AttributeType.FORM;
import static net.ripe.db.whois.common.rpsl.AttributeType.GEOLOC;
import static net.ripe.db.whois.common.rpsl.AttributeType.HOLES;
import static net.ripe.db.whois.common.rpsl.AttributeType.IFADDR;
import static net.ripe.db.whois.common.rpsl.AttributeType.IMPORT;
import static net.ripe.db.whois.common.rpsl.AttributeType.IMPORT_VIA;
import static net.ripe.db.whois.common.rpsl.AttributeType.INET6NUM;
import static net.ripe.db.whois.common.rpsl.AttributeType.INETNUM;
import static net.ripe.db.whois.common.rpsl.AttributeType.INET_RTR;
import static net.ripe.db.whois.common.rpsl.AttributeType.INJECT;
import static net.ripe.db.whois.common.rpsl.AttributeType.INTERFACE;
import static net.ripe.db.whois.common.rpsl.AttributeType.IRT;
import static net.ripe.db.whois.common.rpsl.AttributeType.IRT_NFY;
import static net.ripe.db.whois.common.rpsl.AttributeType.KEY_CERT;
import static net.ripe.db.whois.common.rpsl.AttributeType.LANGUAGE;
import static net.ripe.db.whois.common.rpsl.AttributeType.LAST_MODIFIED;
import static net.ripe.db.whois.common.rpsl.AttributeType.LOCAL_AS;
import static net.ripe.db.whois.common.rpsl.AttributeType.MBRS_BY_REF;
import static net.ripe.db.whois.common.rpsl.AttributeType.MEMBERS;
import static net.ripe.db.whois.common.rpsl.AttributeType.MEMBER_OF;
import static net.ripe.db.whois.common.rpsl.AttributeType.METHOD;
import static net.ripe.db.whois.common.rpsl.AttributeType.MNTNER;
import static net.ripe.db.whois.common.rpsl.AttributeType.MNT_BY;
import static net.ripe.db.whois.common.rpsl.AttributeType.MNT_DOMAINS;
import static net.ripe.db.whois.common.rpsl.AttributeType.MNT_IRT;
import static net.ripe.db.whois.common.rpsl.AttributeType.MNT_LOWER;
import static net.ripe.db.whois.common.rpsl.AttributeType.MNT_NFY;
import static net.ripe.db.whois.common.rpsl.AttributeType.MNT_REF;
import static net.ripe.db.whois.common.rpsl.AttributeType.MNT_ROUTES;
import static net.ripe.db.whois.common.rpsl.AttributeType.MP_DEFAULT;
import static net.ripe.db.whois.common.rpsl.AttributeType.MP_EXPORT;
import static net.ripe.db.whois.common.rpsl.AttributeType.MP_FILTER;
import static net.ripe.db.whois.common.rpsl.AttributeType.MP_IMPORT;
import static net.ripe.db.whois.common.rpsl.AttributeType.MP_MEMBERS;
import static net.ripe.db.whois.common.rpsl.AttributeType.MP_PEER;
import static net.ripe.db.whois.common.rpsl.AttributeType.MP_PEERING;
import static net.ripe.db.whois.common.rpsl.AttributeType.NETNAME;
import static net.ripe.db.whois.common.rpsl.AttributeType.NIC_HDL;
import static net.ripe.db.whois.common.rpsl.AttributeType.NOTIFY;
import static net.ripe.db.whois.common.rpsl.AttributeType.NSERVER;
import static net.ripe.db.whois.common.rpsl.AttributeType.ORG;
import static net.ripe.db.whois.common.rpsl.AttributeType.ORGANISATION;
import static net.ripe.db.whois.common.rpsl.AttributeType.ORG_NAME;
import static net.ripe.db.whois.common.rpsl.AttributeType.ORG_TYPE;
import static net.ripe.db.whois.common.rpsl.AttributeType.ORIGIN;
import static net.ripe.db.whois.common.rpsl.AttributeType.OWNER;
import static net.ripe.db.whois.common.rpsl.AttributeType.PEER;
import static net.ripe.db.whois.common.rpsl.AttributeType.PEERING;
import static net.ripe.db.whois.common.rpsl.AttributeType.PEERING_SET;
import static net.ripe.db.whois.common.rpsl.AttributeType.PERSON;
import static net.ripe.db.whois.common.rpsl.AttributeType.PHONE;
import static net.ripe.db.whois.common.rpsl.AttributeType.PINGABLE;
import static net.ripe.db.whois.common.rpsl.AttributeType.PING_HDL;
import static net.ripe.db.whois.common.rpsl.AttributeType.POEM;
import static net.ripe.db.whois.common.rpsl.AttributeType.POETIC_FORM;
import static net.ripe.db.whois.common.rpsl.AttributeType.REF_NFY;
import static net.ripe.db.whois.common.rpsl.AttributeType.REMARKS;
import static net.ripe.db.whois.common.rpsl.AttributeType.ROLE;
import static net.ripe.db.whois.common.rpsl.AttributeType.ROUTE;
import static net.ripe.db.whois.common.rpsl.AttributeType.ROUTE6;
import static net.ripe.db.whois.common.rpsl.AttributeType.ROUTE_SET;
import static net.ripe.db.whois.common.rpsl.AttributeType.RTR_SET;
import static net.ripe.db.whois.common.rpsl.AttributeType.SIGNATURE;
import static net.ripe.db.whois.common.rpsl.AttributeType.SOURCE;
import static net.ripe.db.whois.common.rpsl.AttributeType.SPONSORING_ORG;
import static net.ripe.db.whois.common.rpsl.AttributeType.STATUS;
import static net.ripe.db.whois.common.rpsl.AttributeType.TECH_C;
import static net.ripe.db.whois.common.rpsl.AttributeType.TEXT;
import static net.ripe.db.whois.common.rpsl.AttributeType.UPD_TO;
import static net.ripe.db.whois.common.rpsl.AttributeType.ZONE_C;

public final class ObjectTemplateWithChanged extends ObjectTemplate {

    private ObjectTemplateWithChanged(final ObjectType objectType, final int orderPosition, final AttributeTemplate... attributeTemplates) {
        super(objectType, orderPosition, attributeTemplates);
    }

    public ObjectTemplateWithChanged() {
        super();
    }

    protected void init() {
        final ArrayList<ObjectTemplateWithChanged> objectTemplates = Lists.newArrayList(

                new ObjectTemplateWithChanged(ObjectType.AS_BLOCK, 7,
                        new AttributeTemplate(AS_BLOCK, MANDATORY, SINGLE, PRIMARY_KEY, LOOKUP_KEY),
                        new AttributeTemplate(DESCR, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(REMARKS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(ORG, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(NOTIFY, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_BY, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_LOWER, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(CHANGED, DEPRECATED, MULTIPLE),
                        new AttributeTemplate(CREATED, GENERATED, SINGLE),
                        new AttributeTemplate(LAST_MODIFIED, GENERATED, SINGLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplateWithChanged(ObjectType.AS_SET, 9,
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
                        new AttributeTemplate(CHANGED, DEPRECATED, MULTIPLE),
                        new AttributeTemplate(CREATED, GENERATED, SINGLE),
                        new AttributeTemplate(LAST_MODIFIED, GENERATED, SINGLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplateWithChanged(ObjectType.AUT_NUM, 8,
                        new AttributeTemplate(AUT_NUM, MANDATORY, SINGLE, PRIMARY_KEY, LOOKUP_KEY),
                        new AttributeTemplate(AS_NAME, MANDATORY, SINGLE),
                        new AttributeTemplate(DESCR, MANDATORY, MULTIPLE),
                        new AttributeTemplate(MEMBER_OF, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(IMPORT_VIA, OPTIONAL, MULTIPLE, USER_ORDER),
                        new AttributeTemplate(IMPORT, OPTIONAL, MULTIPLE, USER_ORDER),
                        new AttributeTemplate(MP_IMPORT, OPTIONAL, MULTIPLE, USER_ORDER),
                        new AttributeTemplate(EXPORT_VIA, OPTIONAL, MULTIPLE, USER_ORDER),
                        new AttributeTemplate(EXPORT, OPTIONAL, MULTIPLE, USER_ORDER),
                        new AttributeTemplate(MP_EXPORT, OPTIONAL, MULTIPLE, USER_ORDER),
                        new AttributeTemplate(DEFAULT, OPTIONAL, MULTIPLE, USER_ORDER),
                        new AttributeTemplate(MP_DEFAULT, OPTIONAL, MULTIPLE, USER_ORDER),
                        new AttributeTemplate(REMARKS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(ORG, OPTIONAL, SINGLE, INVERSE_KEY),
                        new AttributeTemplate(SPONSORING_ORG, OPTIONAL, SINGLE),
                        new AttributeTemplate(ADMIN_C, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(TECH_C, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(STATUS, GENERATED, SINGLE),
                        new AttributeTemplate(NOTIFY, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_LOWER, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_ROUTES, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_BY, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(CHANGED, DEPRECATED, MULTIPLE),
                        new AttributeTemplate(CREATED, GENERATED, SINGLE),
                        new AttributeTemplate(LAST_MODIFIED, GENERATED, SINGLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplateWithChanged(ObjectType.DOMAIN, 30,
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
                        new AttributeTemplate(CHANGED, DEPRECATED, MULTIPLE),
                        new AttributeTemplate(CREATED, GENERATED, SINGLE),
                        new AttributeTemplate(LAST_MODIFIED, GENERATED, SINGLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplateWithChanged(ObjectType.FILTER_SET, 21,
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
                        new AttributeTemplate(CHANGED, DEPRECATED, MULTIPLE),
                        new AttributeTemplate(CREATED, GENERATED, SINGLE),
                        new AttributeTemplate(LAST_MODIFIED, GENERATED, SINGLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplateWithChanged(ObjectType.INET_RTR, 15,
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
                        new AttributeTemplate(CHANGED, DEPRECATED, MULTIPLE),
                        new AttributeTemplate(CREATED, GENERATED, SINGLE),
                        new AttributeTemplate(LAST_MODIFIED, GENERATED, SINGLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplateWithChanged(ObjectType.INET6NUM, 6,
                        new AttributeTemplate(INET6NUM, MANDATORY, SINGLE, PRIMARY_KEY, LOOKUP_KEY),
                        new AttributeTemplate(NETNAME, MANDATORY, SINGLE, LOOKUP_KEY),
                        new AttributeTemplate(DESCR, MANDATORY, MULTIPLE),
                        new AttributeTemplate(COUNTRY, MANDATORY, MULTIPLE),
                        new AttributeTemplate(GEOLOC, OPTIONAL, SINGLE),
                        new AttributeTemplate(LANGUAGE, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(ORG, OPTIONAL, SINGLE, INVERSE_KEY),
                        new AttributeTemplate(SPONSORING_ORG, OPTIONAL, SINGLE),
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
                        new AttributeTemplate(CHANGED, DEPRECATED, MULTIPLE),
                        new AttributeTemplate(CREATED, GENERATED, SINGLE),
                        new AttributeTemplate(LAST_MODIFIED, GENERATED, SINGLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplateWithChanged(ObjectType.INETNUM, 5,
                        new AttributeTemplate(INETNUM, MANDATORY, SINGLE, PRIMARY_KEY, LOOKUP_KEY),
                        new AttributeTemplate(NETNAME, MANDATORY, SINGLE, LOOKUP_KEY),
                        new AttributeTemplate(DESCR, MANDATORY, MULTIPLE),
                        new AttributeTemplate(COUNTRY, MANDATORY, MULTIPLE),
                        new AttributeTemplate(GEOLOC, OPTIONAL, SINGLE),
                        new AttributeTemplate(LANGUAGE, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(ORG, OPTIONAL, SINGLE, INVERSE_KEY),
                        new AttributeTemplate(SPONSORING_ORG, OPTIONAL, SINGLE),
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
                        new AttributeTemplate(CHANGED, DEPRECATED, MULTIPLE),
                        new AttributeTemplate(CREATED, GENERATED, SINGLE),
                        new AttributeTemplate(LAST_MODIFIED, GENERATED, SINGLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplateWithChanged(ObjectType.IRT, 41,
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
                        new AttributeTemplate(CHANGED, DEPRECATED, MULTIPLE),
                        new AttributeTemplate(CREATED, GENERATED, SINGLE),
                        new AttributeTemplate(LAST_MODIFIED, GENERATED, SINGLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplateWithChanged(ObjectType.KEY_CERT, 45,
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
                        new AttributeTemplate(CHANGED, DEPRECATED, MULTIPLE),
                        new AttributeTemplate(CREATED, GENERATED, SINGLE),
                        new AttributeTemplate(LAST_MODIFIED, GENERATED, SINGLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplateWithChanged(ObjectType.MNTNER, 40,
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
                        new AttributeTemplate(CHANGED, DEPRECATED, MULTIPLE),
                        new AttributeTemplate(CREATED, GENERATED, SINGLE),
                        new AttributeTemplate(LAST_MODIFIED, GENERATED, SINGLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplateWithChanged(ObjectType.ORGANISATION, 48,
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
                        new AttributeTemplate(CHANGED, DEPRECATED, MULTIPLE),
                        new AttributeTemplate(CREATED, GENERATED, SINGLE),
                        new AttributeTemplate(LAST_MODIFIED, GENERATED, SINGLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplateWithChanged(ObjectType.PEERING_SET, 22,
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
                        new AttributeTemplate(CHANGED, DEPRECATED, MULTIPLE),
                        new AttributeTemplate(CREATED, GENERATED, SINGLE),
                        new AttributeTemplate(LAST_MODIFIED, GENERATED, SINGLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplateWithChanged(ObjectType.PERSON, 50,
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
                        new AttributeTemplate(CHANGED, DEPRECATED, MULTIPLE),
                        new AttributeTemplate(CREATED, GENERATED, SINGLE),
                        new AttributeTemplate(LAST_MODIFIED, GENERATED, SINGLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplateWithChanged(ObjectType.POEM, 37,
                        new AttributeTemplate(POEM, MANDATORY, SINGLE, PRIMARY_KEY, LOOKUP_KEY),
                        new AttributeTemplate(DESCR, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(FORM, MANDATORY, SINGLE, INVERSE_KEY),
                        new AttributeTemplate(TEXT, MANDATORY, MULTIPLE),
                        new AttributeTemplate(AUTHOR, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(REMARKS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(NOTIFY, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_BY, MANDATORY, SINGLE, INVERSE_KEY),
                        new AttributeTemplate(CHANGED, DEPRECATED, MULTIPLE),
                        new AttributeTemplate(CREATED, GENERATED, SINGLE),
                        new AttributeTemplate(LAST_MODIFIED, GENERATED, SINGLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplateWithChanged(ObjectType.POETIC_FORM, 36,
                        new AttributeTemplate(POETIC_FORM, MANDATORY, SINGLE, PRIMARY_KEY, LOOKUP_KEY),
                        new AttributeTemplate(DESCR, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(ADMIN_C, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(REMARKS, OPTIONAL, MULTIPLE),
                        new AttributeTemplate(NOTIFY, OPTIONAL, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(MNT_BY, MANDATORY, MULTIPLE, INVERSE_KEY),
                        new AttributeTemplate(CHANGED, DEPRECATED, MULTIPLE),
                        new AttributeTemplate(CREATED, GENERATED, SINGLE),
                        new AttributeTemplate(LAST_MODIFIED, GENERATED, SINGLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplateWithChanged(ObjectType.ROLE, 49,
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
                        new AttributeTemplate(CHANGED, DEPRECATED, MULTIPLE),
                        new AttributeTemplate(CREATED, GENERATED, SINGLE),
                        new AttributeTemplate(LAST_MODIFIED, GENERATED, SINGLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplateWithChanged(ObjectType.ROUTE_SET, 12,
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
                        new AttributeTemplate(CHANGED, DEPRECATED, MULTIPLE),
                        new AttributeTemplate(CREATED, GENERATED, SINGLE),
                        new AttributeTemplate(LAST_MODIFIED, GENERATED, SINGLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplateWithChanged(ObjectType.ROUTE, 10,
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
                        new AttributeTemplate(CHANGED, DEPRECATED, MULTIPLE),
                        new AttributeTemplate(CREATED, GENERATED, SINGLE),
                        new AttributeTemplate(LAST_MODIFIED, GENERATED, SINGLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplateWithChanged(ObjectType.ROUTE6, 11,
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
                        new AttributeTemplate(CHANGED, DEPRECATED, MULTIPLE),
                        new AttributeTemplate(CREATED, GENERATED, SINGLE),
                        new AttributeTemplate(LAST_MODIFIED, GENERATED, SINGLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE)),

                new ObjectTemplateWithChanged(ObjectType.RTR_SET, 23,
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
                        new AttributeTemplate(CHANGED, DEPRECATED, MULTIPLE),
                        new AttributeTemplate(CREATED, GENERATED, SINGLE),
                        new AttributeTemplate(LAST_MODIFIED, GENERATED, SINGLE),
                        new AttributeTemplate(SOURCE, MANDATORY, SINGLE))
        );

        final Map<ObjectType, ObjectTemplate> templateMap = Maps.newEnumMap(ObjectType.class);
        for (final ObjectTemplate objectTemplate : objectTemplates) {
            templateMap.put(objectTemplate.getObjectType(), objectTemplate);
        }

        TEMPLATE_MAP = Collections.unmodifiableMap(templateMap);
    }
}
