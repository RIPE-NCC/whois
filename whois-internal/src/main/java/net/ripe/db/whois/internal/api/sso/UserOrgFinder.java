package net.ripe.db.whois.internal.api.sso;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations;
import net.ripe.db.whois.common.dao.jdbc.index.IndexStrategies;
import net.ripe.db.whois.common.dao.jdbc.index.IndexStrategy;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Set;

@Component
public class UserOrgFinder {

    private enum MntByOrRef {MNT_BY, MNT_REF}

    private final Maintainers maintainers;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserOrgFinder(@Qualifier("whoisReadOnlySlaveDataSource") final DataSource dataSource,
                         final Maintainers maintainers) {

        this.maintainers = maintainers;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // auth <- mntner <- org (mnt-by/mnt-ref)
    @Transactional
    public Set<RpslObject> findOrganisationsForAuth(final String auth) {

        final Set<RpslObject> result = Sets.newHashSet();
        for (RpslObjectInfo mntnerId : IndexStrategies.get(AttributeType.AUTH).findInIndex(jdbcTemplate, auth)) {
            result.addAll(findOrgsByMntner(mntnerId, MntByOrRef.MNT_BY));
            result.addAll(findOrgsByMntner(mntnerId, MntByOrRef.MNT_REF));
        }
        return result;
    }

    private Set<RpslObject> findOrgsByMntner(final RpslObjectInfo mntnerId, final MntByOrRef refOrBy) {
        final Set<RpslObject> result = Sets.newHashSet();

        final IndexStrategy strategy = (refOrBy == MntByOrRef.MNT_REF) ?
                IndexStrategies.get(AttributeType.MNT_REF) : IndexStrategies.get(AttributeType.MNT_BY);

        for (RpslObjectInfo orgId : strategy.findInIndex(jdbcTemplate, mntnerId, ObjectType.ORGANISATION)) {
            if (refOrBy == MntByOrRef.MNT_BY ||
                    (refOrBy == MntByOrRef.MNT_REF && isOrgMntByRS(orgId))) {

                result.add(JdbcRpslObjectOperations.getObjectById(jdbcTemplate, orgId.getObjectId()));
            }
        }
        return result;
    }

    private boolean isOrgMntByRS(final RpslObjectInfo orgId) {
        RpslObject org = JdbcRpslObjectOperations.getObjectById(jdbcTemplate, orgId.getObjectId());
        return Sets.intersection(org.getValuesForAttribute(AttributeType.MNT_BY), maintainers.getPowerMaintainers()).size() > 0;
    }
}
