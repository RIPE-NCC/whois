package net.ripe.db.whois.query.dao.jdbc;

import net.ripe.db.whois.common.dao.jdbc.domain.Ipv4EntryMapper;
import net.ripe.db.whois.common.iptree.Ipv4Entry;
import net.ripe.db.whois.query.dao.InetnumDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

// TODO: [AH] replace this early implementation with IndexStrategy
@Repository
public class JdbcInetnumDao implements InetnumDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcInetnumDao(@Qualifier("sourceAwareDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public List<Ipv4Entry> findByNetname(final String netname) {
        return jdbcTemplate.query(
                "SELECT begin_in, end_in, object_id " +
                        "FROM inetnum " +
                        "WHERE netname = ? " +
                        "ORDER BY begin_in ASC, end_in DESC",
                new Ipv4EntryMapper(),
                netname);
    }
}
