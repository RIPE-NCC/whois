package net.ripe.db.whois.internal.api.sso;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.List;
import java.util.Set;

public class AuthService {
    private RpslObjectDao objectDao;
    private RpslObjectUpdateDao updateDao;

    public Set<RpslObject> getOrganisationsForAuth(final String auth) {
        final List<RpslObjectInfo> mntners = objectDao.findByAttribute(AttributeType.AUTH, auth);

        final Set<RpslObject> organisations = Sets.newHashSet();

        for (final RpslObjectInfo mntner : mntners) {
            // TODO is this correct, can there be more than one reference? An alternative is to use getReferences, which gets ALL references
            final RpslObjectInfo attributeReference = updateDao.getAttributeReference(AttributeType.MNT_REF, CIString.ciString(mntner.getKey()));
            if (attributeReference != null && attributeReference.getObjectType() == ObjectType.ORGANISATION) {
                organisations.add(objectDao.getById(attributeReference.getObjectId()));
            }
        }

        return organisations;
    }

    // for testing
    void setObjectDao(final RpslObjectDao objectDao) {
        this.objectDao = objectDao;
    }

    void setUpdateDao(final RpslObjectUpdateDao updateDao) {
        this.updateDao = updateDao;
    }
}
