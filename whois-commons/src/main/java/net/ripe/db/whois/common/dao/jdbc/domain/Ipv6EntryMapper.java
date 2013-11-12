package net.ripe.db.whois.common.dao.jdbc.domain;

import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.iptree.Ipv6Entry;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class Ipv6EntryMapper implements RowMapper<Ipv6Entry> {
    @Override
    public Ipv6Entry mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        return new Ipv6Entry(
                Ipv6Resource.parseFromStrings(rs.getString(1), rs.getString(2), rs.getInt(3)),
                rs.getInt(4));
    }
}
