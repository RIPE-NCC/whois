package net.ripe.db.whois.common.dao;

import net.ripe.commons.ip.Asn;
import net.ripe.commons.ip.AsnRange;
import net.ripe.commons.ip.Ipv4;
import net.ripe.commons.ip.Ipv4Range;
import net.ripe.commons.ip.Ipv6;
import net.ripe.commons.ip.Ipv6Range;
import net.ripe.commons.ip.SortedRangeSet;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RetryFor(RecoverableDataAccessException.class)
@Transactional(isolation = Isolation.READ_COMMITTED)
public class ResourceDataDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ResourceDataDao(@Qualifier("internalsDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public AuthoritativeResource load(final String source) {
        final SortedRangeSet<Asn, AsnRange> autNums = new SortedRangeSet<>();
        final SortedRangeSet<Ipv4, Ipv4Range> inetnums = new SortedRangeSet<>();
        final SortedRangeSet<Ipv6, Ipv6Range> inet6nums = new SortedRangeSet<>();

        jdbcTemplate.query("SELECT resource FROM authoritative_resource WHERE source = ?", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                String resource = rs.getString(1);
                if (resource.startsWith("AS")) {
                    autNums.add(resource.contains("-") ? AsnRange.parse(resource) : Asn.parse(resource).asRange());
                } else if (resource.indexOf(':') >= 0) {
                    inet6nums.add(Ipv6Range.parse(resource));
                } else {
                    inetnums.add(Ipv4Range.parse(resource));
                }
            }

        }, source);
        return new AuthoritativeResource(autNums, inetnums, inet6nums);
    }

    public void store(final String source, final AuthoritativeResource authoritativeResource) {
        jdbcTemplate.update("DELETE FROM authoritative_resource WHERE source = ?", source);

        final List<String> resources = authoritativeResource.getResources();

        jdbcTemplate.batchUpdate("INSERT INTO authoritative_resource (source, resource) VALUES (?, ?)", new BatchPreparedStatementSetter() {
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
