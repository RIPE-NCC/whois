package net.ripe.db.whois.nrtm;

import net.ripe.db.whois.common.domain.IpResourceEntry;
import net.ripe.db.whois.common.domain.IpResourceTree;
import net.ripe.db.whois.common.ip.IpInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class AccessControlList {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessControlList.class);

    private static final int TREE_UPDATE_IN_SECONDS = 600;

    private final JdbcTemplate jdbcTemplate;
    private IpResourceTree<Boolean> mirror;

    @Autowired
    public AccessControlList(@Qualifier("aclDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public boolean isMirror(InetAddress address) {
        return Boolean.TRUE.equals(mirror.getValue(IpInterval.asIpInterval(address)));
    }

    @PostConstruct
    @Scheduled(fixedDelay = TREE_UPDATE_IN_SECONDS * 1000)
    public void reload() {
        try {
            mirror = refreshEntries(loadIpMirror());
        } catch (RuntimeException e) {
            LOGGER.warn("Reload failed due to {}: {}", e.getClass().getName(), e.getMessage());
        }
    }

    public List<IpResourceEntry<Boolean>> loadIpMirror() {
        return jdbcTemplate.query("SELECT prefix FROM acl_mirror", new BooleanEntryMapper());
    }

    private <V> IpResourceTree<V> refreshEntries(final List<IpResourceEntry<V>> entries) {
        final IpResourceTree<V> temp = new IpResourceTree<>();

        for (final IpResourceEntry<V> entry : entries) {
            temp.add(entry.getIpInterval(), entry.getValue());
        }

        return temp;
    }

    private static class BooleanEntryMapper implements RowMapper<IpResourceEntry<Boolean>> {
        @Override
        public IpResourceEntry<Boolean> mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new IpResourceEntry<>(IpInterval.parse(rs.getString(1)), Boolean.TRUE);
        }
    }
}
