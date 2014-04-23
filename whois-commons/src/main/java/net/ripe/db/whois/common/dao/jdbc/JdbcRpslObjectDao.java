package net.ripe.db.whois.common.dao.jdbc;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.jdbc.domain.RpslObjectInfoResultSetExtractor;
import net.ripe.db.whois.common.dao.jdbc.domain.RpslObjectRowMapper;
import net.ripe.db.whois.common.dao.jdbc.index.IndexStrategies;
import net.ripe.db.whois.common.dao.jdbc.index.IndexStrategy;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Identifiable;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.IllegalSourceException;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.CheckForNull;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
@RetryFor(RecoverableDataAccessException.class)
public class JdbcRpslObjectDao implements RpslObjectDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcRpslObjectDao.class);

    private static final List<AttributeType> RELATED_TO_ATTRIBUTES = Lists.newArrayList(AttributeType.ADMIN_C, AttributeType.AUTHOR, AttributeType.ORG, AttributeType.PING_HDL, AttributeType.TECH_C, AttributeType.ZONE_C);

    private final JdbcTemplate jdbcTemplate;
    private final SourceContext sourceContext;

    @Autowired
    public JdbcRpslObjectDao(@Qualifier("sourceAwareDataSource") final DataSource dataSource, final SourceContext sourceContext) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.sourceContext = sourceContext;
    }

    @Override
    public void load(final List<Identifiable> proxy, final List<RpslObject> result) {
        final Map<Integer, RpslObject> loadedObjects = Maps.newHashMapWithExpectedSize(proxy.size());

        Set<Integer> differences = loadObjects(proxy, loadedObjects);
        if (!differences.isEmpty()) {
            final Source originalSource = sourceContext.getCurrentSource();
            LOGGER.info("Objects in source {} not found for ids: {}", originalSource, differences);

            if (originalSource.getType().equals(Source.Type.SLAVE)) {
                final Source masterSource = Source.master(originalSource.getName());
                try {
                    sourceContext.setCurrent(masterSource);
                    differences = loadObjects(proxy, loadedObjects);
                    if (!differences.isEmpty()) {
                        LOGGER.info("Objects in source {} not found for ids: {}", masterSource, differences);
                    }
                } catch (IllegalSourceException e) {
                    LOGGER.debug("Source not configured: {}", masterSource, e);
                } finally {
                    sourceContext.setCurrent(originalSource);
                }
            }
        }

        final List<RpslObject> rpslObjects = Lists.newArrayList(loadedObjects.values());
        Collections.sort(rpslObjects, new Comparator<RpslObject>() {
            final List<Integer> requestedIds = Lists.newArrayList(Iterables.transform(proxy, new Function<Identifiable, Integer>() {
                @Override
                public Integer apply(final Identifiable input) {
                    return input.getObjectId();
                }
            }));

            @Override
            public int compare(final RpslObject o1, final RpslObject o2) {
                return requestedIds.indexOf(o1.getObjectId()) - requestedIds.indexOf(o2.getObjectId());
            }
        });

        // TODO [AK] Return result rather than adding all to the collection
        result.addAll(rpslObjects);
    }

    private Set<Integer> loadObjects(final List<Identifiable> proxy, final Map<Integer, RpslObject> loadedObjects) {
        final StringBuilder queryBuilder = new StringBuilder();
        final List<Integer> objectIds = Lists.newArrayListWithExpectedSize(proxy.size());
        for (final Identifiable identifiable : proxy) {
            final Integer objectId = identifiable.getObjectId();
            if (loadedObjects.containsKey(objectId)) {
                continue;
            }

            if (identifiable instanceof RpslObject) {
                loadedObjects.put(objectId, (RpslObject) identifiable);
            } else {
                if (queryBuilder.length() > 0) {
                    // In MySQL, UNION ALL is much faster than IN
                    queryBuilder.append(" UNION ALL ");
                }

                queryBuilder.append("" +
                        "SELECT object_id, object " +
                        "FROM last " +
                        "WHERE object_id = ? " +
                        "AND sequence_id != 0");

                objectIds.add(objectId);
            }
        }

        final List<RpslObject> rpslObjects = jdbcTemplate.query(
                queryBuilder.toString(),
                new PreparedStatementSetter() {
                    @Override
                    public void setValues(final PreparedStatement ps) throws SQLException {
                        for (int i = 0; i < objectIds.size(); i++) {
                            ps.setInt(i + 1, objectIds.get(i));
                        }
                    }
                },
                new RpslObjectRowMapper());

        for (final RpslObject rpslObject : rpslObjects) {
            loadedObjects.put(rpslObject.getObjectId(), rpslObject);
        }

        if (proxy.size() == loadedObjects.size()) {
            return Collections.emptySet();
        }

        final Set<Integer> differences = Sets.newLinkedHashSet();
        for (final Identifiable identifiable : proxy) {
            final Integer objectId = identifiable.getObjectId();
            if (!loadedObjects.containsKey(objectId)) {
                differences.add(objectId);
            }
        }

        return differences;
    }

    @Override
    public RpslObject getById(final int objectId) {
        return JdbcRpslObjectOperations.getObjectById(jdbcTemplate, objectId);
    }

    @Override
    public RpslObject getByKey(final ObjectType type, final String key) {
        return getById(findByKey(type, key).getObjectId());
    }

    @Override
    public RpslObject getByKeyOrNull(final ObjectType type, final String key) {
        final RpslObjectInfo rpslObjectInfo = findByKeyOrNull(type, key);
        if (rpslObjectInfo == null) {
            return null;
        }
        return getById(rpslObjectInfo.getObjectId());
    }

    @Override
    public RpslObject getByKey(final ObjectType type, final CIString key) {
        return getByKey(type, key.toString());
    }

    @Override
    public RpslObject getByKeyOrNull(final ObjectType type, final CIString key) {
        return getByKeyOrNull(type, key.toString());
    }

    @Override
    public List<RpslObject> getByKeys(final ObjectType type, final Collection<CIString> searchKeys) {
        final List<RpslObject> result = new ArrayList<>(searchKeys.size());

        for (final CIString searchKey : searchKeys) {
            try {
                result.add(getByKey(type, searchKey));
            } catch (EmptyResultDataAccessException ignored) {
            }
        }

        return result;
    }

    @Override
    @CheckForNull
    public RpslObject findAsBlock(final long begin, final long end) {
        final List<RpslObject> asBlock = jdbcTemplate.query("" +
                        "SELECT l.object_id, l.object " +
                        "FROM last l " +
                        "JOIN as_block a ON l.object_id = a.object_id " +
                        "WHERE ? >= a.begin_as " +
                        "AND ? <= a.end_as " +
                        "AND l.sequence_id != 0",
                new RpslObjectRowMapper(),
                begin,
                end
        );

        return asBlock.isEmpty() ? null : asBlock.get(0);
    }

    @Override
    public List<RpslObject> findAsBlockIntersections(final long begin, final long end) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("begin", begin);
        params.put("end", end);

        return new NamedParameterJdbcTemplate(jdbcTemplate).query("" +
                "SELECT l.object_id, l.object " +
                "FROM last l " +
                "JOIN as_block a ON l.object_id = a.object_id " +
                "WHERE (:begin >= a.begin_as AND :begin <=a.end_as) " +
                "OR (:end >= a.begin_as AND :end <=a.end_as) " +
                "OR (:begin<=a.begin_as AND :end >= a.end_as) " +
                "AND l.sequence_id != 0",
                params,
                new RpslObjectRowMapper()
        );
    }

    @Override
    public RpslObjectInfo findByKey(final ObjectType type, final CIString searchKey) {
        return findByKey(type, searchKey.toString());
    }

    @Override
    public RpslObjectInfo findByKeyOrNull(final ObjectType type, final CIString searchKey) {
        return findByKeyOrNull(type, searchKey.toString());
    }

    @Override
    public RpslObjectInfo findByKey(final ObjectType type, final String searchKey) {
        RpslObjectInfo result = findByKeyOrNull(type, searchKey);
        if (result == null) {
            throw new EmptyResultDataAccessException(1);
        } else {
            return result;
        }
    }

    @Override
    public RpslObjectInfo findByKeyOrNull(final ObjectType type, final String searchKey) {
        final List<RpslObjectInfo> objectInfos = findByKeyInIndex(type, searchKey);

        switch (objectInfos.size()) {
            case 0:
                return null;
            case 1:
                return objectInfos.get(0);
            default:
                throw new IncorrectResultSizeDataAccessException(String.format("Multiple objects found in key index for object [%s] %s", type, searchKey), 1, objectInfos.size());
        }
    }

    private List<RpslObjectInfo> findByKeyInIndex(final ObjectType type, final String key) {
        final AttributeType keyLookupAttribute = ObjectTemplate.getTemplate(type).getKeyLookupAttribute();
        final IndexStrategy indexStrategy = IndexStrategies.get(keyLookupAttribute);
        return indexStrategy.findInIndex(jdbcTemplate, key, type);
    }

    @Override
    public List<RpslObjectInfo> findByAttribute(final AttributeType attributeType, final String attributeValue) {
        final IndexStrategy indexStrategy = IndexStrategies.get(attributeType);
        return indexStrategy.findInIndex(jdbcTemplate, attributeValue);
    }

    @Override
    public List<RpslObjectInfo> findMemberOfByObjectTypeWithoutMbrsByRef(final ObjectType objectType, final String attributeValue) {
        final ObjectTemplate objectTemplate = ObjectTemplate.getTemplate(objectType);
        final Set<AttributeType> keyAttributes = objectTemplate.getKeyAttributes();
        Validate.isTrue(keyAttributes.size() == 1);

        final IndexStrategy indexStrategy = IndexStrategies.get(keyAttributes.iterator().next());

        final String query = MessageFormat.format(
                "SELECT l.object_id, l.object_type, l.pkey " +
                        "  FROM {0}, member_of" +
                        "  LEFT OUTER JOIN mnt_by ON member_of.object_id = mnt_by.object_id " +
                        "  LEFT JOIN last l ON l.object_id = member_of.object_id " +
                        "  WHERE {0}.object_id = member_of.set_id " +
                        "  AND {0}.{1} = ?" +
                        "  AND l.sequence_id != 0 ",
                indexStrategy.getLookupTableName(),
                indexStrategy.getLookupColumnName());

        return jdbcTemplate.query(query, new RpslObjectInfoResultSetExtractor(), attributeValue);
    }

    @Override
    public Collection<RpslObjectInfo> relatedTo(final RpslObject identifiable, final Set<ObjectType> excludeObjectTypes) {
        final LinkedHashSet<RpslObjectInfo> result = Sets.newLinkedHashSet();

        for (final RpslAttribute attribute : identifiable.findAttributes(RELATED_TO_ATTRIBUTES)) {
            for (final CIString referenceValue : attribute.getReferenceValues()) {
                for (final ObjectType objectType : attribute.getType().getReferences(referenceValue)) {
                    if (excludeObjectTypes.contains(objectType)) {
                        continue;
                    }

                    for (RpslObjectInfo rpslObjectInfo : findByKeyInIndex(objectType, referenceValue.toString())) {
                        if (rpslObjectInfo.getObjectId() != identifiable.getObjectId()) {
                            result.add(rpslObjectInfo);
                        }
                    }
                }
            }
        }

        return result;
    }
}
