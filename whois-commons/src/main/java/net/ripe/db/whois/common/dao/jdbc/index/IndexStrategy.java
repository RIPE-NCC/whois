package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public interface IndexStrategy {

    AttributeType getAttributeType();

    int addToIndex(JdbcTemplate jdbcTemplate, RpslObjectInfo objectInfo, RpslObject object, CIString value);

    int addToIndex(JdbcTemplate jdbcTemplate, RpslObjectInfo objectInfo, RpslObject object, String value);

    List<RpslObjectInfo> findInIndex(JdbcTemplate jdbcTemplate, String value);

    List<RpslObjectInfo> findInIndex(JdbcTemplate jdbcTemplate, CIString value);

    void removeFromIndex(JdbcTemplate jdbcTemplate, RpslObjectInfo objectInfo);

    String getLookupTableName();

    String getLookupColumnName();

    void cleanupMissingObjects(JdbcTemplate jdbcTemplate);
}
