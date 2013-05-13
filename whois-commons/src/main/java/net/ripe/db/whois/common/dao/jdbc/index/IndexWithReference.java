package net.ripe.db.whois.common.dao.jdbc.index;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.collect.CollectionHelper;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.dao.jdbc.domain.RpslObjectResultSetExtractor;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.lang.Validate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.annotation.CheckForNull;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Set;

class IndexWithReference extends IndexStrategySimpleLookup {

    IndexWithReference(final AttributeType attributeType, final String lookupTableName, final String lookupColumnName) {
        super(attributeType, lookupTableName, lookupColumnName);
    }

    private Set<IndexStrategy> referenceStrategies = null;

    private Set<IndexStrategy> getReferenceStrategies() {
        if (referenceStrategies == null) {
            final Set<ObjectType> references = attributeType.getReferences();
            Validate.notEmpty(references, "No references for: " + attributeType);

            final Set<IndexStrategy> tmpReferenceStrategies = Sets.newHashSet();
            for (final ObjectType reference : references) {
                final ObjectTemplate template = ObjectTemplate.getTemplate(reference);
                for (final AttributeType keyAttribute : template.getKeyAttributes()) {
                    tmpReferenceStrategies.add(IndexStrategies.get(keyAttribute));
                }
            }

            referenceStrategies = tmpReferenceStrategies;
        }

        return referenceStrategies;
    }

    @CheckForNull
    RpslObjectInfo getReference(final JdbcTemplate jdbcTemplate, final String pkey) {
        for (final IndexStrategy referenceStrategy : getReferenceStrategies()) {
            final RpslObjectInfo result = CollectionHelper.uniqueResult(referenceStrategy.findInIndex(jdbcTemplate, pkey));
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    @Override
    public int addToIndex(final JdbcTemplate jdbcTemplate, final RpslObjectInfo objectInfo, final RpslObject object, final String value) {
        final RpslObjectInfo reference = getReference(jdbcTemplate, value);
        if (reference == null) {
            throw new IllegalArgumentException("Referenced object does not exist: " + value);
        }

        return addToIndex(jdbcTemplate, objectInfo, reference);
    }

    int addToIndex(final JdbcTemplate jdbcTemplate, final RpslObjectInfo objectInfo, final RpslObjectInfo reference) {
        return addToIndex(jdbcTemplate, objectInfo, reference.getObjectId());
    }

    int addToIndex(final JdbcTemplate jdbcTemplate, final RpslObjectInfo objectInfo, final int referenceObjectId) {
        final String query = String.format("INSERT INTO %s (object_id, %s, object_type) VALUES (?, ?, ?)", lookupTableName, lookupColumnName);
        return jdbcTemplate.update(query, objectInfo.getObjectId(), referenceObjectId, ObjectTypeIds.getId(objectInfo.getObjectType()));
    }

    @Override
    public List<RpslObjectInfo> findInIndex(final JdbcTemplate jdbcTemplate, final String value) {
        final Set<Integer> ids = Sets.newHashSet();

        for (final IndexStrategy referenceStrategy : getReferenceStrategies()) {
            for (final RpslObjectInfo rpslObjectInfo : referenceStrategy.findInIndex(jdbcTemplate, value)) {
                ids.add(rpslObjectInfo.getObjectId());
            }
        }

        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        final String query = MessageFormat.format(
                "SELECT l.object_id, l.object_type, l.pkey " +
                        "  FROM {0} " +
                        "  LEFT JOIN last l ON l.object_id = {0}.object_id " +
                        "  WHERE {0}.{1} in (:ids) " +
                        "  AND l.sequence_id != 0 ",
                lookupTableName,
                lookupColumnName);

        return new NamedParameterJdbcTemplate(jdbcTemplate).query(
                query,
                new MapSqlParameterSource("ids", ids),
                new RpslObjectResultSetExtractor());
    }
}
