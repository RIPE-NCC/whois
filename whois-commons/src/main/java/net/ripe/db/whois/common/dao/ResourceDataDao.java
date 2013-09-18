package net.ripe.db.whois.common.dao;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.domain.Ipv6Resource;
import net.ripe.db.whois.common.etree.IntervalMap;
import net.ripe.db.whois.common.etree.NestedIntervalMap;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

@Repository
@RetryFor(RecoverableDataAccessException.class)
public class ResourceDataDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ResourceDataDao(@Qualifier("internalsDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Transactional
    public AuthoritativeResource load(final Logger logger, final String source) {
        final Set<CIString> autNums = Sets.newHashSet();
        final IntervalMap<Ipv4Resource, Ipv4Resource> inetnums = new NestedIntervalMap<>();
        final IntervalMap<Ipv6Resource, Ipv6Resource> inet6nums = new NestedIntervalMap<>();

        jdbcTemplate.query("SELECT resource FROM authoritative_resource WHERE source = ?", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                String resource = rs.getString(1);
                if (resource.startsWith("AS")) {
                    autNums.add(CIString.ciString(resource));
                } else if (resource.indexOf(':') >= 0) {
                    Ipv6Resource ipv6Resource = Ipv6Resource.parseIPv6Resource(resource);
                    inet6nums.put(ipv6Resource, ipv6Resource);
                } else {
                    Ipv4Resource ipv4Resource = Ipv4Resource.parseIPv4Resource(resource);
                    inetnums.put(ipv4Resource, ipv4Resource);
                }
            }
        }, source);
        return new AuthoritativeResource(logger, autNums, inetnums, inet6nums);
    }

    @Transactional
    public void store(final String source, final AuthoritativeResource authoritativeResource) {
        jdbcTemplate.update("DELETE FROM authoritative_resource WHERE source = ?", source);

        final List<String> resources = authoritativeResource.getResources();

        jdbcTemplate.batchUpdate("INSERT INTO authoritative_resource VALUES (?, ?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(final PreparedStatement ps, final int i) throws SQLException {
                ps.setString(1, source);
                ps.setString(2, resources.get(i));
            }

            @Override
            public int getBatchSize() {
                return resources.size();
            }
        });
    }
}
