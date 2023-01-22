package net.ripe.db.whois.common.dao.jdbc;

import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import jakarta.sql.DataSource;

import static net.ripe.db.whois.common.dao.jdbc.JdbcStreamingHelper.executeStreaming;

@Repository
@RetryFor(RecoverableDataAccessException.class)
public class JdbcManagedAttributeDao {

    private final JdbcTemplate jdbcTemplate;
    private final Maintainers maintainers;

    @Autowired
    public JdbcManagedAttributeDao(@Qualifier("whoisSlaveDataSource") final DataSource dataSource,
                                   final Maintainers maintainers) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.maintainers = maintainers;
    }

    public boolean hasManagedResource(final CIString orgId) {
        final int orgObjectId = jdbcTemplate.queryForObject(
            "SELECT object_id FROM organisation WHERE organisation = ?",
                Integer.class,
                new Object[] { orgId }
        );

        return executeStreaming(
                jdbcTemplate,
                "SELECT l.object " +
                    "FROM   last l INNER JOIN org o ON l.object_id = o.object_id " +
                    "WHERE  o.org_id = ? " +
                    "AND    o.object_type in (?, ?, ?) " +
                    "AND    l.sequence_id > 0",
                pstmt -> {
                    pstmt.setInt(1, orgObjectId);
                    pstmt.setInt(2, ObjectTypeIds.getId(ObjectType.INETNUM));
                    pstmt.setInt(3, ObjectTypeIds.getId(ObjectType.INET6NUM));
                    pstmt.setInt(4, ObjectTypeIds.getId(ObjectType.AUT_NUM));
                },
                resultSet -> {
                    while (resultSet.next()) {
                        if (hasRipeNccMntner(RpslObject.parse(resultSet.getString("object")))) {
                            return true;
                        }
                    }
                    return false;
                }
        );
    }

    private boolean hasRipeNccMntner(final RpslObject rpslObject) {
        return maintainers.isRsMaintainer(rpslObject.getValuesForAttribute(AttributeType.MNT_BY));
    }

}
