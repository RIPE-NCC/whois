package net.ripe.db.whois.common.dao.jdbc;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.collect.CollectionHelper;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.dao.jdbc.domain.RpslObjectRowMapper;
import net.ripe.db.whois.common.dao.jdbc.index.IndexStrategies;
import net.ripe.db.whois.common.dao.jdbc.index.IndexStrategy;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.CheckForNull;
import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.copyToHistoryAndUpdateSerials;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.deleteFromLastAndUpdateSerials;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.deleteFromTables;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.insertIntoLastAndUpdateSerials;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.insertIntoTables;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.insertIntoTablesIgnoreMissing;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.lookupRpslObjectUpdateInfo;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.updateLastAndUpdateSerials;
import static net.ripe.db.whois.common.domain.CIString.ciString;

@Repository
@Transactional
public class JdbcRpslObjectUpdateDao implements RpslObjectUpdateDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcRpslObjectUpdateDao.class);

    private final JdbcTemplate jdbcTemplate;
    private final DateTimeProvider dateTimeProvider;

    @Autowired
    public JdbcRpslObjectUpdateDao(@Qualifier("sourceAwareDataSource") final DataSource dataSource,
                                   final DateTimeProvider dateTimeProvider) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dateTimeProvider = dateTimeProvider;
    }

    @Override
    public boolean isReferenced(final RpslObject object) {
        for (final RpslAttribute attribute : object.findAttributes(ObjectTemplate.getTemplate(object.getType()).getKeyAttributes())) {
            for (final IndexStrategy indexStrategy : IndexStrategies.getReferencing(object.getType())) {
                for (final CIString value : attribute.getReferenceValues()) {
                    for (final RpslObjectInfo result : indexStrategy.findInIndex(jdbcTemplate, value)) {
                        if (object.getKey().equals(ciString(result.getKey())) && result.getObjectType().equals(object.getType())) {
                            continue;
                        }

                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public Set<RpslObjectInfo> getReferences(final RpslObject object) {
        Set<RpslObjectInfo> references = Sets.newHashSet();
        final List<IndexStrategy> indexStrategies = IndexStrategies.getReferencing(object.getType());

        // for route(6), individually check each key
        for (final RpslAttribute keyAttr : object.findAttributes(ObjectTemplate.getTemplate(object.getType()).getKeyAttributes())) {
            for (final IndexStrategy indexStrategy : indexStrategies) {
                for (final CIString value : keyAttr.getReferenceValues()) {
                    final List<RpslObjectInfo> results = indexStrategy.findInIndex(jdbcTemplate, value);
                    for (final RpslObjectInfo result : results) {
                        if (object.getKey().equals(result.getKey()) && result.getObjectType().equals(object.getType())) {
                            continue;
                        }

                        references.add(result);
                    }
                }
            }
        }

        return references;
    }

    @Override
    public Map<RpslAttribute, Set<CIString>> getInvalidReferences(final RpslObject object) {
        final Map<RpslAttribute, Set<CIString>> invalidReferenceMap = Maps.newHashMap();

        for (final RpslAttribute attribute : object.getAttributes()) {
            final Set<CIString> invalidReferenceValues = getInvalidReferences(object, attribute);
            if (!invalidReferenceValues.isEmpty()) {
                invalidReferenceMap.put(attribute, invalidReferenceValues);
            }
        }

        return invalidReferenceMap;
    }

    private Set<CIString> getInvalidReferences(final RpslObject object, final RpslAttribute attribute) {
        AttributeType attributeType = attribute.getType();
        if (attributeType != null && attributeType.getReferences().isEmpty()) {
            return Collections.emptySet();
        }

        final Set<CIString> invalidReferences = Sets.newLinkedHashSet();
        for (final CIString value : attribute.getReferenceValues()) {
            if (isInvalidReference(object, attributeType, value)) {
                invalidReferences.add(value);
            }
        }

        return invalidReferences;
    }

    private boolean isInvalidReference(final RpslObject object, final AttributeType attributeType, final CIString referenceValue) {
        final Set<ObjectType> references = attributeType.getReferences(referenceValue);
        if (references.isEmpty()) {
            return false;
        }

        for (final ObjectType reference : references) {
            if (reference.equals(object.getType()) && object.getKey().equals(referenceValue)) {
                return false;
            }

            if (getAttributeReference(reference, referenceValue) != null) {
                return false;
            }
        }

        return true;
    }

    @CheckForNull
    public RpslObjectInfo getAttributeReference(final AttributeType attributeType, final CIString value) {
        final CIString referenceValue = new RpslAttribute(attributeType, value.toString()).getReferenceValue();
        for (final ObjectType objectType : attributeType.getReferences()) {
            final RpslObjectInfo result = getAttributeReference(objectType, referenceValue);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private RpslObjectInfo getAttributeReference(final ObjectType objectType, final CIString keyValue) {
        final ObjectTemplate referenceTemplate = ObjectTemplate.getTemplate(objectType);
        final Set<AttributeType> referenceKeyAttributes = referenceTemplate.getKeyAttributes();
        Validate.isTrue(referenceKeyAttributes.size() == 1, "We can never have a reference to a composed key");
        final IndexStrategy indexStrategy = IndexStrategies.get(referenceKeyAttributes.iterator().next());
        final List<RpslObjectInfo> result = indexStrategy.findInIndex(jdbcTemplate, keyValue);
        return CollectionHelper.uniqueResult(result);
    }

    @Override
    public RpslObjectUpdateInfo deleteObject(final int objectId, final String pkey) {
        final RpslObjectUpdateInfo rpslObjectInfo = lookupRpslObjectUpdateInfo(jdbcTemplate, objectId, pkey);

        deleteFromTables(jdbcTemplate, rpslObjectInfo);
        copyToHistoryAndUpdateSerials(jdbcTemplate, rpslObjectInfo);
        deleteFromLastAndUpdateSerials(dateTimeProvider, jdbcTemplate, rpslObjectInfo);

        return new RpslObjectUpdateInfo(rpslObjectInfo.getObjectId(), 0, rpslObjectInfo.getObjectType(), rpslObjectInfo.getKey());
    }

    @Override
    public RpslObjectUpdateInfo undeleteObject(final int objectId) {
        final RpslObject rpslObject = jdbcTemplate.queryForObject(
                "select h.object_id, h.object " +
                "from last l " +
                "left join history h on l.object_id = h.object_id " +
                "where l.object_id = ? " +
                "and l.sequence_id = 0 " +
                "and h.timestamp in (select max(h2.timestamp) from history h2 where h2.object_id = ?)",
                new RpslObjectRowMapper(),
                objectId, objectId);

        final int sequenceId = jdbcTemplate.queryForObject(
                "select max(sequence_id) from serials where object_id = ?",
                Integer.class,
                objectId);

        final ObjectType objectType = rpslObject.getType();
        final String pkey = rpslObject.getKey().toString();
        final RpslObjectUpdateInfo updateInfo = new RpslObjectUpdateInfo(objectId, sequenceId, objectType, pkey);

        final Set<CIString> missingReferences = insertIntoTablesIgnoreMissing(jdbcTemplate, updateInfo, rpslObject);
        if (!missingReferences.isEmpty()) {
            LOGGER.warn("Missing references undeleting object {}: {}", objectId, missingReferences);
        }

        final int newSequenceId = updateLastAndUpdateSerials(dateTimeProvider, jdbcTemplate, updateInfo, rpslObject);
        return new RpslObjectUpdateInfo(objectId, newSequenceId, objectType, pkey);
    }

    @Override
    public RpslObjectUpdateInfo updateObject(final int objectId, final RpslObject object) {
        final RpslObjectUpdateInfo rpslObjectInfo = lookupRpslObjectUpdateInfo(jdbcTemplate, objectId, object.getKey().toString());

        deleteFromTables(jdbcTemplate, rpslObjectInfo);
        insertIntoTables(jdbcTemplate, rpslObjectInfo, object);
        copyToHistoryAndUpdateSerials(jdbcTemplate, rpslObjectInfo);
        final int newSequenceId = updateLastAndUpdateSerials(dateTimeProvider, jdbcTemplate, rpslObjectInfo, object);

        return new RpslObjectUpdateInfo(rpslObjectInfo.getObjectId(), newSequenceId, rpslObjectInfo.getObjectType(), rpslObjectInfo.getKey());
    }

    @Override
    public RpslObjectUpdateInfo createObject(final RpslObject object) {
        final RpslObjectUpdateInfo rpslObjectInfo = insertIntoLastAndUpdateSerials(dateTimeProvider, jdbcTemplate, object);

        insertIntoTables(jdbcTemplate, rpslObjectInfo, object);
        return rpslObjectInfo;
    }

    @Override
    public RpslObjectUpdateInfo lookupObject(ObjectType type, String pkey) {
        return lookupRpslObjectUpdateInfo(jdbcTemplate, type, pkey);
    }
}
