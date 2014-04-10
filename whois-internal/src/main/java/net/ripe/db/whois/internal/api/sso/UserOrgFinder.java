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
            result.addAll(findOrgsByMntner(mntnerId, AttributeType.MNT_BY));
            result.addAll(findOrgsByMntner(mntnerId, AttributeType.MNT_REF));
        }
        return result;
    }

    private Set<RpslObject> findOrgsByMntner(final RpslObjectInfo mntnerId, final AttributeType refOrBy) {
        final Set<RpslObject> result = Sets.newHashSet();
        final IndexStrategy strategy = IndexStrategies.get(refOrBy);

        for (RpslObjectInfo orgId : strategy.findInIndex(jdbcTemplate, mntnerId, ObjectType.ORGANISATION)) {
            if (refOrBy == AttributeType.MNT_BY || (refOrBy == AttributeType.MNT_REF && isOrgMntByRS(orgId))) {
                result.add(JdbcRpslObjectOperations.getObjectById(jdbcTemplate, orgId));
            }
        }
        return result;
    }

    private boolean isOrgMntByRS(final RpslObjectInfo orgId) {
        RpslObject org = JdbcRpslObjectOperations.getObjectById(jdbcTemplate, orgId);
        return !Sets.intersection(org.getValuesForAttribute(AttributeType.MNT_BY), maintainers.getRsMaintainers()).isEmpty();
    }
}
