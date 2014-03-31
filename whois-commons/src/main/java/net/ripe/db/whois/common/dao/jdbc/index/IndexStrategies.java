package net.ripe.db.whois.common.dao.jdbc.index;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.apache.commons.lang.Validate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class IndexStrategies {
    private static final Map<AttributeType, IndexStrategy> INDEX_BY_ATTRIBUTE;
    private static final Map<ObjectType, List<IndexStrategy>> INDEXES_REFERING_OBJECT;

    static {
        final IndexStrategy[] indexStrategies = {
                new IndexWithReference(AttributeType.ABUSE_C, "abuse_c", "pe_ro_id"),
                new IndexWithValueAndType(AttributeType.ABUSE_MAILBOX, "abuse_mailbox", "abuse_mailbox"),
                new Unindexed(AttributeType.ADDRESS),
                new IndexWithReference(AttributeType.ADMIN_C, "admin_c", "pe_ro_id"),
                new Unindexed(AttributeType.AGGR_BNDRY),
                new Unindexed(AttributeType.AGGR_MTD),
                new Unindexed(AttributeType.ALIAS),
                new Unindexed(AttributeType.ASSIGNMENT_SIZE),
                new IndexWithAsBlock(AttributeType.AS_BLOCK),
                new Unindexed(AttributeType.AS_NAME),
                new IndexWithValue(AttributeType.AS_SET, "as_set", "as_set"),
                new IndexWithAuth(AttributeType.AUTH, "auth", "auth"),
                new IndexWithReference(AttributeType.AUTHOR, "author", "pe_ro_id"),
                new IndexWithValue(AttributeType.AUT_NUM, "aut_num", "aut_num"),
                new Unindexed(AttributeType.CERTIF),
                new Unindexed(AttributeType.CHANGED),
                new Unindexed(AttributeType.COMPONENTS),
                new Unindexed(AttributeType.COUNTRY),
                new Unindexed(AttributeType.DEFAULT),
                new Unindexed(AttributeType.DESCR),
                new IndexWithValue(AttributeType.DOMAIN, "domain", "domain"),
                new IndexWithValue(AttributeType.DS_RDATA, "ds_rdata", "ds_rdata"),
                new Unindexed(AttributeType.ENCRYPTION),
                new Unindexed(AttributeType.EXPORT),
                new Unindexed(AttributeType.EXPORT_VIA),
                new Unindexed(AttributeType.EXPORT_COMPS),
                new IndexWithValueAndType(AttributeType.E_MAIL, "e_mail", "e_mail"),
                new Unindexed(AttributeType.FAX_NO),
                new Unindexed(AttributeType.FILTER),
                new IndexWithValue(AttributeType.FILTER_SET, "filter_set", "filter_set"),
                new IndexWithValue(AttributeType.FINGERPR, "fingerpr", "fingerpr"),
                new IndexWithReference(AttributeType.FORM, "form", "form_id"),
                new Unindexed(AttributeType.GEOLOC),
                new Unindexed(AttributeType.HOLES),
                new IndexWithIfAddr(AttributeType.IFADDR),
                new Unindexed(AttributeType.IMPORT),
                new Unindexed(AttributeType.IMPORT_VIA),
                new IndexWithInet6num(AttributeType.INET6NUM),
                new IndexWithInetnum(AttributeType.INETNUM),
                new IndexWithValue(AttributeType.INET_RTR, "inet_rtr", "inet_rtr"),
                new Unindexed(AttributeType.INJECT),
                new Unindexed(AttributeType.INTERFACE),
                new IndexWithValue(AttributeType.IRT, "irt", "irt"),
                new IndexWithValue(AttributeType.IRT_NFY, "irt_nfy", "irt_nfy"),
                new IndexWithValue(AttributeType.KEY_CERT, "key_cert", "key_cert"),
                new Unindexed(AttributeType.LANGUAGE),
                new IndexWithLocalAs(AttributeType.LOCAL_AS),
                new IndexWithReference(AttributeType.MBRS_BY_REF, "mbrs_by_ref", "mnt_id"),
                new Unindexed(AttributeType.MEMBERS),
                new IndexWithMemberOf(AttributeType.MEMBER_OF),
                new Unindexed(AttributeType.METHOD),
                new IndexWithMaintainer(AttributeType.MNTNER, "mntner", "mntner"),
                new IndexWithReference(AttributeType.MNT_BY, "mnt_by", "mnt_id"),
                new IndexWithReference(AttributeType.MNT_DOMAINS, "mnt_domains", "mnt_id"),
                new IndexWithReference(AttributeType.MNT_IRT, "mnt_irt", "irt_id"),
                new IndexWithReference(AttributeType.MNT_LOWER, "mnt_lower", "mnt_id"),
                new IndexWithValue(AttributeType.MNT_NFY, "mnt_nfy", "mnt_nfy"),
                new IndexWithReference(AttributeType.MNT_REF, "mnt_ref", "mnt_id"),
                new IndexWithMntRoutes(AttributeType.MNT_ROUTES),
                new Unindexed(AttributeType.MP_DEFAULT),
                new Unindexed(AttributeType.MP_EXPORT),
                new Unindexed(AttributeType.MP_FILTER),
                new Unindexed(AttributeType.MP_IMPORT),
                new Unindexed(AttributeType.MP_MEMBERS),
                new Unindexed(AttributeType.MP_PEER),
                new Unindexed(AttributeType.MP_PEERING),
                new Unindexed(AttributeType.NETNAME),
                new IndexWithValueAndType(AttributeType.NIC_HDL, "person_role", "nic_hdl"),
                new IndexWithValueAndType(AttributeType.NOTIFY, "notify", "notify"),
                new IndexWithNServer(AttributeType.NSERVER, "nserver", "host"),
                new IndexWithReference(AttributeType.ORG, "org", "org_id"),
                new Unindexed(AttributeType.ORG_TYPE),
                new IndexWithValue(AttributeType.ORGANISATION, "organisation", "organisation"),
                new IndexWithName(AttributeType.ORG_NAME, "org_name"),
                new IndexWithOrigin(AttributeType.ORIGIN),
                new Unindexed(AttributeType.OWNER),
                new Unindexed(AttributeType.PEER),
                new Unindexed(AttributeType.PEERING),
                new IndexWithValue(AttributeType.PEERING_SET, "peering_set", "peering_set"),
                new IndexWithNameAndType(AttributeType.PERSON, ObjectType.PERSON, "names"),
                new Unindexed(AttributeType.PHONE),
                new IndexWithReference(AttributeType.PING_HDL, "ping_hdl", "pe_ro_id"),
                new Unindexed(AttributeType.PINGABLE),
                new IndexWithValue(AttributeType.POEM, "poem", "poem"),
                new IndexWithValue(AttributeType.POETIC_FORM, "poetic_form", "poetic_form"),
                new IndexWithReference(AttributeType.REFERRAL_BY, "referral_by", "mnt_id"),
                new IndexWithValue(AttributeType.REF_NFY, "ref_nfy", "ref_nfy"),
                new Unindexed(AttributeType.REMARKS),
                new IndexWithNameAndType(AttributeType.ROLE, ObjectType.ROLE, "names"),
                new IndexWithRoute(AttributeType.ROUTE),
                new IndexWithRoute6(AttributeType.ROUTE6),
                new IndexWithValue(AttributeType.ROUTE_SET, "route_set", "route_set"),
                new IndexWithValue(AttributeType.RTR_SET, "rtr_set", "rtr_set"),
                new Unindexed(AttributeType.SIGNATURE),
                new Unindexed(AttributeType.SOURCE),
                new IndexWithReference(AttributeType.SPONSORING_ORG, "sponsoring_org", "org_id"),
                new Unindexed(AttributeType.STATUS),
                new IndexWithReference(AttributeType.TECH_C, "tech_c", "pe_ro_id"),
                new Unindexed(AttributeType.TEXT),
                new IndexWithValue(AttributeType.UPD_TO, "upd_to", "upd_to"),
                new IndexWithReference(AttributeType.ZONE_C, "zone_c", "pe_ro_id")
        };

        final Map<AttributeType, IndexStrategy> indexByAttribute = Maps.newEnumMap(AttributeType.class);
        for (final IndexStrategy indexStrategy : indexStrategies) {
            final AttributeType attributeType = indexStrategy.getAttributeType();
            final IndexStrategy previous = indexByAttribute.put(attributeType, indexStrategy);
            Validate.isTrue(previous == null, "Multiple definitions for: " + attributeType);
        }
        INDEX_BY_ATTRIBUTE = Collections.unmodifiableMap(indexByAttribute);

        final Map<ObjectType, List<IndexStrategy>> indexesReferingObject = Maps.newEnumMap(ObjectType.class);
        for (final ObjectType objectType : ObjectType.values()) {
            final List<IndexStrategy> indexesRefererringCurrentObject = Lists.newArrayList();
            for (final IndexStrategy indexStrategy : indexStrategies) {
                if (indexStrategy.getAttributeType().getReferences().contains(objectType)) {
                    indexesRefererringCurrentObject.add(indexStrategy);
                }
            }

            indexesReferingObject.put(objectType, Collections.unmodifiableList(indexesRefererringCurrentObject));
        }
        INDEXES_REFERING_OBJECT = Collections.unmodifiableMap(indexesReferingObject);
    }

    private IndexStrategies() {
    }

    public static IndexStrategy get(final AttributeType attributeType) {
        return INDEX_BY_ATTRIBUTE.get(attributeType);
    }

    public static List<IndexStrategy> getReferencing(final ObjectType objectType) {
        return INDEXES_REFERING_OBJECT.get(objectType);
    }
}
