package net.ripe.db.whois.query.dao.jdbc;

import net.ripe.db.whois.common.dao.jdbc.domain.Ipv6EntryMapper;
import net.ripe.db.whois.common.iptree.Ipv6Entry;
import net.ripe.db.whois.query.dao.Inet6numDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

// TODO: [AH] replace this early implementation with IndexStrategy
@Repository
public class JdbcInet6numDao implements Inet6numDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcInet6numDao(@Qualifier("sourceAwareDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public List<Ipv6Entry> findByNetname(final String netname) {
        return jdbcTemplate.query(
                "SELECT i6_msb, i6_lsb, prefix_length, object_id " +
                        "FROM inet6num " +
                        "WHERE netname = ? " +
                        "ORDER BY i6_msb, i6_lsb, prefix_length ASC",
                new Ipv6EntryMapper(),
                netname);
    }
}
