package net.ripe.db.whois.common.dao.jdbc.domain;

import net.ripe.db.whois.common.dao.VersionInfo;
import net.ripe.db.whois.common.domain.serials.Operation;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class VersionInfoRowMapper implements RowMapper<VersionInfo> {
    @Override
    public VersionInfo mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        return new VersionInfo(
                rs.getBoolean(1), //inLast
                rs.getInt(2), // objectId
                rs.getInt(3), //sequenceId
                rs.getLong(5), //timestamp
                Operation.getByCode(rs.getInt(4))); //operation
    }
}