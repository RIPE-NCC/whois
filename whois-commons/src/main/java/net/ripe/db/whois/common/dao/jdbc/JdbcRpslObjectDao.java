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
import net.ripe.db.whois.common.dao.jdbc.domain.RpslObjectResultSetExtractor;
import net.ripe.db.whois.common.dao.jdbc.domain.RpslObjectRowMapper;
import net.ripe.db.whois.common.dao.jdbc.index.IndexStrategies;
import net.ripe.db.whois.common.dao.jdbc.index.IndexStrategy;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.*;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.common.source.SourceNotConfiguredException;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;

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
    // TODO [AK] Make loader smarter, sometimes proxy already has some objects of result (object already loaded)
    public void load(final List<Integer> proxy, final List<RpslObject> result) {
        final StringBuilder queryBuilder = new StringBuilder();
        final Integer[] objectIds = new Integer[proxy.size()];
        int idx = 0;

        // In MySQL, UNION ALL is much faster than IN
        for (final Integer objectId : proxy) {
            if (idx > 0) {
                queryBuilder.append(" UNION ALL ");

            }
            queryBuilder.append("" +
                    "SELECT object_id, object " +
                    "FROM last " +
                    "WHERE object_id = ? " +
                    "AND sequence_id != 0");
            objectIds[idx++] = objectId;
        }

        final String loadObjectsQuery = queryBuilder.toString();
        final RpslObjectRowMapper rowMapper = new RpslObjectRowMapper();

        List<RpslObject> objects = jdbcTemplate.query(loadObjectsQuery, objectIds, rowMapper);
        Set<Integer> differences = getDifferences(proxy, objects);
        if (!differences.isEmpty()) {
            final Source originalSource = sourceContext.getCurrentSource();
            LOGGER.warn("Objects in source {} not found for ids: {}", originalSource, differences);

            if (originalSource.getType().equals(Source.Type.SLAVE)) {
                final Source masterSource = Source.master(originalSource.getName());
                try {
                    sourceContext.setCurrent(masterSource);
                    objects = jdbcTemplate.query(loadObjectsQuery, objectIds, rowMapper);
                    differences = getDifferences(proxy, objects);
                    if (!differences.isEmpty()) {
                        LOGGER.warn("Objects in source {} not found for ids: {}", masterSource, differences);
                    }
                } catch (SourceNotConfiguredException e) {
                    LOGGER.debug("Source not configured: {}", masterSource, e);
                } finally {
                    sourceContext.setCurrent(originalSource);
                }
            }
        }

        result.addAll(objects);
    }

    private Set<Integer> getDifferences(final Collection<Integer> proxy, final Collection<RpslObject> objects) {
        if (proxy.size() == objects.size()) {
            return Collections.emptySet();
        }

        final Set<Integer> requestedIds = Sets.newHashSet(proxy);
        final Set<Integer> foundObjectIds = Sets.newHashSet(Iterables.transform(objects, new Function<RpslObject, Integer>() {
            @Nullable
            @Override
            public Integer apply(final RpslObject input) {
                return input.getObjectId();
            }
        }));

        return Sets.difference(requestedIds, foundObjectIds);
    }

    @Override
    public RpslObject getById(final int objectId) {
        return jdbcTemplate.queryForObject("" +
                "SELECT object_id, object FROM last " +
                "WHERE object_id = ? " +
                "AND sequence_id != 0",
                new RpslObjectRowMapper(),
                objectId);
    }

    @Override
    public RpslObject getByKey(final ObjectType type, final String key) {
        RpslObject rpslObject;
        try {
            rpslObject = jdbcTemplate.queryForObject("" +
                    "SELECT object_id, object " +
                    "  FROM last " +
                    "  WHERE object_type = ? and pkey = ? and sequence_id != 0 ",
                    new RpslObjectRowMapper(),
                    ObjectTypeIds.getId(type),
                    key);
        } catch (EmptyResultDataAccessException e) {
            rpslObject = null;
        }

        final RpslObject objectWithIndexFallback = getObjectWithIndexFallback(rpslObject, type, ciString(key));
        if (objectWithIndexFallback == null) {
            throw new EmptyResultDataAccessException(1);
        }

        return objectWithIndexFallback;
    }

    private RpslObject getObjectWithIndexFallback(final RpslObject object, final ObjectType type, final CIString key) {
        if (object != null) {
            return object;
        }

        final Set<RpslObjectInfo> objectInfos = Sets.newHashSet();
        final ObjectTemplate objectTemplate = ObjectTemplate.getTemplate(type);
        for (final AttributeType attributeType : objectTemplate.getKeyAttributes()) {
            objectInfos.addAll(IndexStrategies.get(attributeType).findInIndex(jdbcTemplate, key));
        }

        switch (objectInfos.size()) {
            case 0:
                return null;
            case 1:
                final RpslObjectInfo objectInfo = objectInfos.iterator().next();
                if (objectInfo.getObjectType().equals(type)) {
                    LOGGER.warn("Object [{}] {} exists in key index, but not found in last by pkey", type, key);
                    return getById(objectInfo.getObjectId());
                } else {
                    return null;
                }
            default:
                throw new IncorrectResultSizeDataAccessException(String.format("Multiple objects found in key index for object [%s] %s", type, key), 1, objectInfos.size());
        }
    }

    @Override
    public List<RpslObject> getByKeys(final ObjectType type, final Collection<CIString> searchKeys) {
        if (searchKeys.isEmpty()) {
            return Collections.emptyList();
        }

        final Map<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("objectType", ObjectTypeIds.getId(type));

        final Set<String> result = Sets.newHashSet();
        for (final CIString value : searchKeys) {
            result.add(value.toString());
        }

        paramMap.put("pkeys", result);

        final Map<CIString, RpslObject> rpslObjectMap = new NamedParameterJdbcTemplate(jdbcTemplate).query("" +
                "SELECT object_id, object, pkey " +
                "  FROM last " +
                "  WHERE object_type = :objectType and pkey in (:pkeys) and sequence_id != 0 ",
                paramMap,
                new ResultSetExtractor<Map<CIString, RpslObject>>() {
                    @Override
                    public Map<CIString, RpslObject> extractData(final ResultSet rs) throws SQLException, DataAccessException {
                        final Map<CIString, RpslObject> result = Maps.newHashMap();
                        final RpslObjectRowMapper rowMapper = new RpslObjectRowMapper();

                        int rowNum = 0;
                        while (rs.next()) {
                            result.put(ciString(rs.getString(3)), rowMapper.mapRow(rs, rowNum++));
                        }

                        return result;
                    }
                });


        final Set<RpslObject> results = Sets.newLinkedHashSetWithExpectedSize(rpslObjectMap.size());
        for (final CIString searchKey : searchKeys) {
            final RpslObject objectWithIndexFallback = getObjectWithIndexFallback(rpslObjectMap.get(searchKey), type, searchKey);
            if (objectWithIndexFallback != null) {
                results.add(objectWithIndexFallback);
            }
        }

        return Lists.newArrayList(results);
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

    // TODO [AK] This is inefficient, loading the object only to get rid of the RPSL data, should be the other way around, don't forget about index fallback
    @Override
    public RpslObjectInfo findByKey(final ObjectType type, final String searchKey) {
        final RpslObject rpslObject = getByKey(type, searchKey);
        return new RpslObjectInfo(rpslObject.getObjectId(), rpslObject.getType(), rpslObject.getKey());
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

        return jdbcTemplate.query(query, new RpslObjectResultSetExtractor(), attributeValue);
    }

    @Override
    public List<RpslObjectInfo> relatedTo(final RpslObject identifiable) {
        final Set<RpslObjectInfo> result = Sets.newLinkedHashSet();
        for (final AttributeType attributeType : RELATED_TO_ATTRIBUTES) { // TODO [AK] Pass in related-to attributes to optimise --no-personal
            final List<RpslAttribute> attributes = identifiable.findAttributes(attributeType);
            for (final RpslAttribute attribute : attributes) {
                for (final CIString referenceValue : attribute.getReferenceValues()) {
                    for (final ObjectType objectType : attributeType.getReferences(referenceValue)) {
                        for (AttributeType keyAttributeType : ObjectTemplate.getTemplate(objectType).getKeyAttributes()) {
                            final List<RpslObjectInfo> objectInfos = IndexStrategies.get(keyAttributeType).findInIndex(jdbcTemplate, referenceValue);
                            for (final RpslObjectInfo objectInfo : objectInfos) {
                                if (objectInfo.getObjectId() != identifiable.getObjectId()) {
                                    result.add(objectInfo);
                                }
                            }
                        }
                    }
                }
            }
        }

        return Lists.newArrayList(result);
    }
}
