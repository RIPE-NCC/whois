package net.ripe.db.whois.common.dao.jdbc;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.dao.jdbc.domain.RpslObjectRowMapper;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
@RetryFor(RecoverableDataAccessException.class)
public class BulkRpslReadOnlyLoader {

    public static final int BATCH_SIZE = 1_000;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public BulkRpslReadOnlyLoader(@Qualifier("whoisSlaveDataSource") final DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(new JdbcTemplate(dataSource));
    }

    public List<RpslObject> getByObjectIds(final List<Integer> objectIds) {
        if (objectIds.isEmpty()) {
            return Collections.emptyList();
        }

        final Map<Integer, RpslObject> loadedMap = new HashMap<>();
        Lists.partition(objectIds, BATCH_SIZE).forEach(subList -> {
            loadByIds(subList).forEach(obj -> loadedMap.put(obj.getObjectId(), obj));
        });

        final List<RpslObject> ordered = new ArrayList<>(objectIds.size());
        for (Integer id : objectIds) {
            RpslObject obj = loadedMap.get(id);
            if (obj != null) {
                ordered.add(obj);
            }
        }

        return ordered;
    }

    public List<RpslObject> getByKeys(final List<CIString> keys, final ObjectType... objectType) {
        if (keys.isEmpty() || objectType.length == 0) {
            return Collections.emptyList();
        }

        final Map<CIString, RpslObject> loadedMap = new HashMap<>();
        Lists.partition(keys, BATCH_SIZE).forEach(subList -> {
            loadByKey(subList, objectType)
                    .forEach(obj -> loadedMap.put(obj.getKey(), obj));
        });

        final List<RpslObject> ordered = new ArrayList<>(keys.size());
        for (CIString key : keys) {
            RpslObject obj = loadedMap.get(key);
            if (obj != null) {
                ordered.add(obj);
            }
        }

        return ordered;
    }

    private List<RpslObject> loadByIds(final List<Integer> objectIds) {

        final Map<String, Object> params = new HashMap<>();
        params.put("objectIds", objectIds);

        return namedParameterJdbcTemplate.query(
                        "SELECT object_id, object FROM last WHERE object_id IN (:objectIds) AND sequence_id != 0",
                        params,
                        new RpslObjectRowMapper());
    }

    private List<RpslObject> loadByKey(final List<CIString> keys, final ObjectType... objectType) {
        if (keys.isEmpty() || objectType.length == 0) {
            return Collections.emptyList();
        }

        final Map<String, Object> params = new HashMap<>();
        params.put("keys", keys.stream().map(CIString::toString).collect(Collectors.toList()));
        params.put("objectTypes", Stream.of(objectType).map(ObjectTypeIds::getId).collect(Collectors.toSet()));
        return namedParameterJdbcTemplate.query(
                "SELECT object_id, object FROM last WHERE pkey IN (:keys) AND sequence_id != 0 and object_type IN (:objectTypes)",
                params,
                new RpslObjectRowMapper());
    }
}
