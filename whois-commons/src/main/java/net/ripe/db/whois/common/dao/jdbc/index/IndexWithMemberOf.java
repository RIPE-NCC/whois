package net.ripe.db.whois.common.dao.jdbc.index;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.jdbc.domain.RpslObjectResultSetExtractor;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.jdbc.core.JdbcTemplate;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

import static net.ripe.db.whois.common.collect.CollectionHelper.uniqueResult;

class IndexWithMemberOf extends IndexWithReference {
    IndexWithMemberOf(final AttributeType attributeType) {
        super(attributeType, "member_of", "set_id");
    }

    @Override
    public int addToIndex(final JdbcTemplate jdbcTemplate, final RpslObjectInfo objectInfo, final RpslObject object, final String value) {
        final IndexStrategy referenceStrategy = IndexStrategies.get(getReferenceAttribute(object.getType()));
        final RpslObjectInfo reference = uniqueResult(referenceStrategy.findInIndex(jdbcTemplate, value));
        if (reference == null) {
            throw new IllegalArgumentException("Referenced object does not exist: " + value);
        }

        return addToIndex(jdbcTemplate, objectInfo, reference);
    }

    @Override
    public List<RpslObjectInfo> findInIndex(final JdbcTemplate jdbcTemplate, final String value) {
        final List<RpslObjectInfo> result = Lists.newArrayList();

        for (final IndexStrategy referenceStrategy : getPossibleReferenceStrategies(value)) {
            result.addAll(findInIndex(jdbcTemplate, value, referenceStrategy));
        }

        return result;
    }

    private List<RpslObjectInfo> findInIndex(final JdbcTemplate jdbcTemplate, final String value, final IndexStrategy referenceStrategy) {
        final String query = MessageFormat.format("" +
                "SELECT l.object_id, l.object_type, l.pkey\n" +
                "        FROM mbrs_by_ref, {0}, {2}\n" +
                "        LEFT JOIN last l ON l.object_id = {2}.object_id " +
                "        LEFT OUTER JOIN mnt_by ON {2}.object_id = mnt_by.object_id\n" +
                "        WHERE (mbrs_by_ref.mnt_id = 0 OR mbrs_by_ref.mnt_id = mnt_by.mnt_id)\n" +
                "        AND {2}.{3} = mbrs_by_ref.object_id\n" +
                "        AND {0}.object_id = {2}.{3}\n" +
                "        AND {0}.{1} = ?\n" +
                "        AND l.sequence_id != 0 ",
                referenceStrategy.getLookupTableName(),
                referenceStrategy.getLookupColumnName(),
                lookupTableName,
                lookupColumnName);

        return jdbcTemplate.query(query, new RpslObjectResultSetExtractor(), value);
    }

    private AttributeType getReferenceAttribute(final ObjectType objectType) {
        switch (objectType) {
            case AUT_NUM:
                return AttributeType.AS_SET;
            case ROUTE:
            case ROUTE6:
                return AttributeType.ROUTE_SET;
            case INET_RTR:
                return AttributeType.RTR_SET;
            default:
                throw new IllegalArgumentException("Unexpected object type: " + objectType);
        }
    }

    private Set<IndexStrategy> getPossibleReferenceStrategies(final String value) {
        final Set<IndexStrategy> referenceStrategies = Sets.newLinkedHashSet();

        for (final ObjectType objectType : attributeType.getReferences()) {
            for (final AttributeType keyAttribute : ObjectTemplate.getTemplate(objectType).getKeyAttributes()) {
                if (keyAttribute.isValidValue(objectType, value)) {
                    referenceStrategies.add(IndexStrategies.get(keyAttribute));
                }
            }
        }

        return referenceStrategies;
    }
}
