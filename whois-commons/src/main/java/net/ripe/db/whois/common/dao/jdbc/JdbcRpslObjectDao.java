package net.ripe.db.whois.common.dao.jdbc;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
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
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.CheckForNull;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciString;

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
        return getByKey(type, ciString(key));
    }

    @Override
    public RpslObject getByKey(final ObjectType type, final CIString key) {
        // TODO: [AH] skip slow lookup on last, use getByKeyFromIndex right away
        try {
            return jdbcTemplate.queryForObject("" +
                    "SELECT object_id, object " +
                    "  FROM last " +
                    "  WHERE object_type = ? and pkey = ? and sequence_id != 0 ",
                    new RpslObjectRowMapper(),
                    ObjectTypeIds.getId(type),
                    key.toString());
        } catch (EmptyResultDataAccessException e) {
            return getByKeyFromIndex(type, key);
        }
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

    private RpslObject getByKeyFromIndex(final ObjectType type, final CIString key) {
        return getById(findUniqueByKeyInIndex(type, key).getObjectId());
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
                end);

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
    public RpslObjectInfo findByKey(final ObjectType type, final String searchKey) {
        // TODO: [AH] skip slow lookup on last, use findUniqueByKeyInIndex right away
        try {
            return jdbcTemplate.queryForObject("" +
                    "SELECT object_id, pkey " +
                    "  FROM last " +
                    "  WHERE object_type = ? and pkey = ? and sequence_id != 0 ",
                    new RowMapper<RpslObjectInfo>() {
                        @Override
                        public RpslObjectInfo mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                            return new RpslObjectInfo(rs.getInt(1), type, rs.getString(2));
                        }
                    },
                    ObjectTypeIds.getId(type),
                    searchKey);
        } catch (EmptyResultDataAccessException e) {
            return findUniqueByKeyInIndex(type, ciString(searchKey));
        }
    }

    private RpslObjectInfo findUniqueByKeyInIndex(final ObjectType type, final CIString key) {
        final Set<RpslObjectInfo> objectInfos = findByKeyInIndex(type, key);

        switch (objectInfos.size()) {
            case 0:
                throw new EmptyResultDataAccessException(1);
            case 1:
                return objectInfos.iterator().next();
            default:
                throw new IncorrectResultSizeDataAccessException(String.format("Multiple objects found in key index for object [%s] %s", type, key), 1, objectInfos.size());
        }
    }

    private Set<RpslObjectInfo> findByKeyInIndex(final ObjectType type, final CIString key) {
        final Set<RpslObjectInfo> objectInfos = Sets.newHashSetWithExpectedSize(1);
        final ObjectTemplate objectTemplate = ObjectTemplate.getTemplate(type);

        // FIXME: [AH] this would erroneously try to match the individual key of route(6) object, instead of matching against the combined key
        for (final AttributeType attributeType : objectTemplate.getKeyAttributes()) {
            final List<RpslObjectInfo> rpslObjectInfos = IndexStrategies.get(attributeType).findInIndex(jdbcTemplate, key);
            for (final RpslObjectInfo rpslObjectInfo : rpslObjectInfos) {

                // Make sure the object type actually matches the requested type, can otherwise fail e.g. when looking up person/role
                if (rpslObjectInfo.getObjectType().equals(type)) {
                    objectInfos.add(rpslObjectInfo);
                }
            }
        }

        return objectInfos;
    }

    @Override
    public List<RpslObjectInfo> findByAttribute(final AttributeType attributeType, final String attributeValue) {
        return IndexStrategies.get(attributeType).findInIndex(jdbcTemplate, attributeValue);
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
    public List<RpslObjectInfo> relatedTo(final RpslObject identifiable, final Set<ObjectType> excludeObjectTypes) {
        final Set<RpslObjectInfo> result = Sets.newLinkedHashSet();

        final List<RpslAttribute> attributes = identifiable.findAttributes(RELATED_TO_ATTRIBUTES);
        for (final RpslAttribute attribute : attributes) {
            for (final CIString referenceValue : attribute.getReferenceValues()) {
                for (final ObjectType objectType : attribute.getType().getReferences(referenceValue)) {
                    if (excludeObjectTypes.contains(objectType)) {
                        continue;
                    }

                    for (final RpslObjectInfo objectInfo : findByKeyInIndex(objectType, referenceValue)) {
                        if (objectInfo.getObjectId() != identifiable.getObjectId()) {
                            result.add(objectInfo);
                        }
                    }
                }
            }
        }

        return Lists.newArrayList(result);
    }
}
