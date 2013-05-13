package net.ripe.db.whois.api.acl;

import net.ripe.db.whois.common.DateTimeProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static net.ripe.db.whois.common.domain.BlockEvent.Type;

@Repository
class AclServiceDao {
    private final JdbcTemplate aclTemplate;
    private final DateTimeProvider dateTimeProvider;

    @Autowired
    AclServiceDao(@Qualifier("aclDataSource") final DataSource dataSource, final DateTimeProvider dateTimeProvider) {
        this.dateTimeProvider = dateTimeProvider;
        this.aclTemplate = new JdbcTemplate(dataSource);
    }

    public List<Limit> getLimits() {
        return aclTemplate.query(
                "SELECT prefix, comment, daily_limit, unlimited_connections FROM acl_limit",
                new RowMapper<Limit>() {
                    @Override
                    public Limit mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                        return new Limit(
                                rs.getString(1),
                                rs.getString(2),
                                rs.getInt(3),
                                rs.getBoolean(4)
                        );
                    }
                }
        );
    }

    public void createLimit(final Limit limit) {
        aclTemplate.update(
                "INSERT INTO acl_limit (prefix, daily_limit, unlimited_connections, comment) VALUES (?, ?, ?, ?)",
                limit.getPrefix(),
                limit.getPersonObjectLimit(),
                limit.isUnlimitedConnections(),
                limit.getComment());
    }

    public void updateLimit(final Limit limit) {
        aclTemplate.update(
                "UPDATE acl_limit SET daily_limit = ?, unlimited_connections = ?, comment = ? WHERE prefix = ?",
                limit.getPersonObjectLimit(),
                limit.isUnlimitedConnections(),
                limit.getComment(),
                limit.getPrefix());
    }

    public void deleteLimit(final String prefix) {
        aclTemplate.update("DELETE FROM acl_limit where prefix = ?", prefix);
    }

    public List<Ban> getBans() {
        return aclTemplate.query(
                "SELECT prefix, comment, denied_date FROM acl_denied ORDER BY denied_date DESC",
                new RowMapper<Ban>() {
                    @Override
                    public Ban mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                        return new Ban(
                                rs.getString(1),
                                rs.getString(2),
                                rs.getDate(3)
                        );
                    }
                }
        );
    }

    public Ban getBan(final String prefix) {
        return aclTemplate.queryForObject(
                "SELECT prefix, comment, denied_date FROM acl_denied WHERE prefix = ?",
                new RowMapper<Ban>() {
                    @Override
                    public Ban mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                        return new Ban(
                                rs.getString(1),
                                rs.getString(2),
                                rs.getDate(3)
                        );
                    }
                },
                prefix
        );
    }

    public void createBan(final Ban ban) {
        aclTemplate.update("INSERT INTO acl_denied (prefix, comment, denied_date) VALUES (?, ?, ?)",
                ban.getPrefix(), ban.getComment(), dateTimeProvider.getCurrentDate().toDate());
    }

    public void updateBan(final Ban ban) {
        aclTemplate.update("UPDATE acl_denied SET comment = ? WHERE prefix = ?",
                ban.getComment(), ban.getPrefix());
    }

    public void deleteBan(final String prefix) {
        aclTemplate.update("DELETE FROM acl_denied where prefix = ?", prefix);
    }

    public void createBanEvent(final String prefix, final Type type) {
        aclTemplate.update("INSERT INTO acl_event (prefix, event_time, event_type, daily_limit) VALUES (?, ?, ?, 0)",
                prefix, dateTimeProvider.getCurrentDateTime().toDate(), type.name());
    }

    public List<BanEvent> getBanEvents(final String prefix) {
        return aclTemplate.query(
                "SELECT prefix, event_time, event_type, daily_limit FROM acl_event WHERE prefix = ? ORDER BY event_time DESC",
                new RowMapper<BanEvent>() {
                    @Override
                    public BanEvent mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                        return new BanEvent(
                                rs.getString(1),
                                rs.getTimestamp(2),
                                Type.valueOf(rs.getString(3)),
                                rs.getInt(4)
                        );
                    }
                },
                prefix
        );
    }

    public List<Proxy> getProxies() {
        return aclTemplate.query(
                "SELECT prefix, comment FROM acl_proxy",
                new RowMapper<Proxy>() {
                    @Override
                    public Proxy mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                        return new Proxy(rs.getString(1), rs.getString(2));
                    }
                }
        );
    }

    public Proxy getProxy(final String prefix) {
        return aclTemplate.queryForObject(
                "SELECT prefix, comment FROM acl_proxy WHERE prefix = ?",
                new RowMapper<Proxy>() {
                    @Override
                    public Proxy mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                        return new Proxy(rs.getString(1), rs.getString(2));
                    }
                },
                prefix
        );
    }

    public void createProxy(final Proxy proxy) {
        aclTemplate.update("INSERT INTO acl_proxy (prefix, comment) VALUES (?, ?)",
                proxy.getPrefix(), proxy.getComment());
    }

    public void updateProxy(final Proxy proxy) {
        aclTemplate.update("UPDATE acl_proxy SET comment = ? WHERE prefix = ?",
                proxy.getComment(), proxy.getPrefix());
    }

    public void deleteProxy(final String prefix) {
        aclTemplate.update("DELETE FROM acl_proxy WHERE prefix = ?", prefix);
    }

    public List<Mirror> getMirrors() {
        return aclTemplate.query("SELECT prefix, comment FROM acl_mirror",
                new RowMapper<Mirror>() {
                    @Override
                    public Mirror mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                        return new Mirror(
                                rs.getString(1),
                                rs.getString(2)
                        );
                    }
                }
        );
    }

    public Mirror getMirror(final String prefix) {
        return aclTemplate.queryForObject(
                "SELECT prefix, comment FROM acl_mirror WHERE prefix = ?",
                new RowMapper<Mirror>() {
                    @Override
                    public Mirror mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                        return new Mirror(rs.getString(1), rs.getString(2));
                    }
                },
                prefix
        );
    }

    public void createMirror(final Mirror mirror) {
        aclTemplate.update("INSERT INTO acl_mirror (prefix, comment) VALUES (?, ?)",
                mirror.getPrefix(), mirror.getComment());
    }

    public void updateMirror(final Mirror mirror) {
        aclTemplate.update("UPDATE acl_mirror SET comment = ? WHERE prefix = ?",
                mirror.getComment(), mirror.getPrefix());
    }

    public void deleteMirror(final String prefix) {
        aclTemplate.update("DELETE FROM acl_mirror WHERE prefix = ?", prefix);
    }
}
