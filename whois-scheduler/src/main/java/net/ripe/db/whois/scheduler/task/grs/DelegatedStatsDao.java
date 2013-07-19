package net.ripe.db.whois.scheduler.task.grs;

import net.ripe.db.whois.common.domain.IpInterval;
import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.domain.Ipv6Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class DelegatedStatsDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DelegatedStatsDao(@Qualifier("delegatedStatsDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<String> findSourceForResource(final String searchValue) {
        final String sql = "SELECT source FROM delegated_stats WHERE type = ? AND resource_start >= ? AND resource_end <= ?";
        Object[] parameters;

        if (searchValue.startsWith("AS")) {
            final String asNumber = searchValue.substring(2);
            parameters = new Object[]{ResourceType.ASN.name(), asNumber, asNumber};

        } else {
            final IpInterval<?> ipInterval = IpInterval.parse(searchValue);
            if (ipInterval instanceof Ipv4Resource) {
                final Ipv4Resource resource = (Ipv4Resource)ipInterval;
                parameters = new Object[]{ResourceType.IPV4.name(), resource.begin(), resource.end()};
            } else {
                final Ipv6Resource resource = (Ipv6Resource)ipInterval;
                parameters = new Object[]{ResourceType.IPV6.name(), resource.begin(), resource.end()};
            }
        }
        System.out.println(sql);
        return jdbcTemplate.queryForList(sql, parameters, String.class);
    }

    enum ResourceType {
        IPV4, IPV6, ASN
    }
}
