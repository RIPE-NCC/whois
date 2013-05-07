package net.ripe.db.whois.common.dao.jdbc.domain;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public final class RpslObjectResultSetExtractor implements ResultSetExtractor<List<RpslObjectInfo>> {
    private final RpslObjectInfoMapper rpslObjectInfoMapper = new RpslObjectInfoMapper();

    @Override
    public List<RpslObjectInfo> extractData(final ResultSet rs) throws SQLException, DataAccessException {
        final List<RpslObjectInfo> rpslObjectInfos = Lists.newArrayList();

        int rowNum = 0;
        while (rs.next()) {
            final RpslObjectInfo rpslObjectInfo = rpslObjectInfoMapper.mapRow(rs, rowNum++);
            if (rpslObjectInfo != null) {
                rpslObjectInfos.add(rpslObjectInfo);
            }
        }

        return rpslObjectInfos;
    }
}
