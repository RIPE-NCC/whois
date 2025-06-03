package net.ripe.db.whois.common.dao;

import net.ripe.commons.ip.Asn;
import net.ripe.commons.ip.AsnRange;
import net.ripe.commons.ip.Ipv4;
import net.ripe.commons.ip.Ipv4Range;
import net.ripe.commons.ip.Ipv6;
import net.ripe.commons.ip.Ipv6Range;
import net.ripe.commons.ip.SortedRangeSet;
import net.ripe.db.whois.common.TransactionConfiguration;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Repository
@RetryFor(RecoverableDataAccessException.class)
@Transactional(transactionManager = TransactionConfiguration.INTERNALS_UPDATE_TRANSACTION, isolation = Isolation.READ_COMMITTED)
public class ResourceDataDao {

    private static TransactionTemplate transactionTemplate;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ResourceDataDao(@Qualifier("internalsDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.transactionTemplate = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
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
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus status) {
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
        });
    }

    public State getState(final String source) {
        return jdbcTemplate.queryForObject("SELECT max(id), count(*) FROM authoritative_resource WHERE source = ?",
            new RowMapper<State>() {
                @Override
                public State mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return new State(source, rs.getInt(1), rs.getInt(2));
                }
            },
            source);
    }

    public static class State implements Comparable<State> {
        private final String source;
        private final int id;
        private final int count;

        public State(final String source, final int id, final int count) {
            this.source = source;
            this.id = id;
            this.count = count;
        }

        @Override
        public int compareTo(final State other) {
            if (!other.source.equals(source)) {
                throw new IllegalArgumentException("Sources are not the same");
            }

            if ((other.id > id) || (other.count > count)) {
                return -1;
            } else {
                if ((other.id != id) || (other.count != count)) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final State state = (State) o;

            return Objects.equals(id, state.id) &&
                    Objects.equals(count, state.count) &&
                    Objects.equals(source, state.source);
        }

        @Override
        public int hashCode() {
            return Objects.hash(source, id, count);
        }
    }
}
