package net.ripe.db.whois.common.dao.jdbc;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.dao.StatusDao;
import net.ripe.db.whois.common.domain.CIString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import jakarta.sql.DataSource;
import java.util.List;
import java.util.Map;

@Repository
@RetryFor(RecoverableDataAccessException.class)
public class JdbcStatusDao implements StatusDao {

    // partition query values to keep inside the max_packet_size limit for the IN clause
    private static final int QUERY_PARTITION = 10_000;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcStatusDao(@Qualifier("sourceAwareDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public CIString getStatus(final int objectId) {
        return jdbcTemplate.queryForObject(
            "SELECT status FROM status WHERE object_id = ?",
            (rs, rowNum) -> CIString.ciString(rs.getString(1)),
            objectId);
    }

    @Override
    public Map<Integer, CIString> getStatus(final List<Integer> objectIds) {
        final Map<Integer, CIString> results = Maps.newHashMap();
        final Map<String, Object> params = Maps.newHashMap();

        for (List<Integer> partition : Lists.partition(objectIds, QUERY_PARTITION)) {
            params.put("objectids", partition);
            namedParameterJdbcTemplate.query(
                "SELECT object_id, status FROM status WHERE object_id IN (:objectids)",
                params,
                resultSet -> {
                    results.put(resultSet.getInt(1), CIString.ciString(resultSet.getString(2)));
                });
        }

        return results;
    }
}
