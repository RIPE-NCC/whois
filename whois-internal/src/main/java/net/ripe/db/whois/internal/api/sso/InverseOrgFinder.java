package net.ripe.db.whois.internal.api.sso;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.ClockDateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectDao;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectUpdateDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Set;

@Component
public class InverseOrgFinder {
    private RpslObjectDao objectDao;
    private RpslObjectUpdateDao updateDao;

    @Autowired
    public InverseOrgFinder(@Qualifier("whoisReadOnlySlaveDataSource") final DataSource dataSource) {
        // TODO: [AH] autowire
        objectDao = new JdbcRpslObjectDao(dataSource, null);
        updateDao = new JdbcRpslObjectUpdateDao(dataSource, new ClockDateTimeProvider());
    }


    public Set<RpslObject> findOrganisationsForAuth(final String auth) {
        final List<RpslObjectInfo> mntners = objectDao.findByAttribute(AttributeType.AUTH, auth);

        final Set<RpslObject> organisations = Sets.newHashSet();

        for (final RpslObjectInfo mntner : mntners) {
            final Set<RpslObjectInfo> references = updateDao.getReferences(objectDao.getByKey(mntner.getObjectType(), mntner.getKey()));
            for (final RpslObjectInfo reference : references) {
                if (reference.getObjectType() == ObjectType.ORGANISATION) {
                    final RpslObject organisation = objectDao.getById(reference.getObjectId());
                    if (organisation.getValuesForAttribute(AttributeType.MNT_BY).contains(CIString.ciString(mntner.getKey()))) {
                        organisations.add(organisation);
                    }
                }
            }
        }

        return organisations;
    }

    // for testing TODO: drop this once fields are autowired
    InverseOrgFinder() {}

    void setObjectDao(final RpslObjectDao objectDao) {
        this.objectDao = objectDao;
    }

    void setUpdateDao(final RpslObjectUpdateDao updateDao) {
        this.updateDao = updateDao;
    }

    RpslObjectDao getObjectDao() {
        return objectDao;
    }

    RpslObjectUpdateDao getUpdateDao() {
        return updateDao;
    }
}
