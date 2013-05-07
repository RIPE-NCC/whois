package net.ripe.db.whois.common.dao.jdbc.domain;

import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RpslObjectRowMapper implements RowMapper<RpslObject> {
    @Override
    public RpslObject mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        return RpslObject.parse(rs.getInt(1), rs.getBytes(2));
    }
}
