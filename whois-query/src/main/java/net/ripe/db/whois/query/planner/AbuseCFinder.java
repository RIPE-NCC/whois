package net.ripe.db.whois.query.planner;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.collect.CollectionHelper;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ip.Ipv4Resource;
import net.ripe.db.whois.common.domain.ip.Ipv6Resource;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.iptree.IpEntry;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
class AbuseCFinder {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbuseCFinder.class);

    private final RpslObjectDao objectDao;
    private final Ipv4Tree ipv4Tree;
    private final Ipv6Tree ipv6Tree;
    private final Maintainers maintainers;

    @Autowired
    public AbuseCFinder(final RpslObjectDao objectDao, final Ipv4Tree ipv4Tree, final Ipv6Tree ipv6Tree, final Maintainers maintainers) {
        this.objectDao = objectDao;
        this.ipv4Tree = ipv4Tree;
        this.ipv6Tree = ipv6Tree;
        this.maintainers = maintainers;
    }

    public Map<CIString, CIString> getAbuseContacts(final RpslObject object) {
        Collection<CIString> abuseContacts = getAbuseMailboxes(object);

        if (abuseContacts.isEmpty() && object.getType() != ObjectType.AUT_NUM) {
            RpslObject parentObject = object;

            while (abuseContacts.isEmpty()) {
                List<? extends IpEntry> parent = Lists.newArrayList();
                if (parentObject.getType() == ObjectType.INETNUM) {
                    parent = ipv4Tree.findFirstLessSpecific(Ipv4Resource.parse(parentObject.getKey()));
                } else if (parentObject.getType() == ObjectType.INET6NUM) {
                    parent = ipv6Tree.findFirstLessSpecific(Ipv6Resource.parse(parentObject.getKey()));
                }

                final IpEntry ipEntry = CollectionHelper.uniqueResult(parent);
                if (ipEntry == null) {
                    break;
                }

                try {
                    parentObject = objectDao.getById(ipEntry.getObjectId());
                } catch (EmptyResultDataAccessException e) {
                    LOGGER.warn("Parent does not exist: {}", ipEntry.getObjectId());
                    break;
                }

                abuseContacts = getAbuseMailboxes(parentObject);

                if (isMaintainedByRs(parentObject)) {
                    break;
                }
            }
        }

        final Iterator<CIString> iterator = abuseContacts.iterator();
        final Map<CIString, CIString> objectKeyWithAbuseContact = Maps.newHashMap();
        if (iterator.hasNext()) {
            objectKeyWithAbuseContact.put(object.getKey(), iterator.next());
        }
        return objectKeyWithAbuseContact;
    }

    private Collection<CIString> getAbuseMailboxes(final RpslObject object) {
        final Set<CIString> orgAttributes = object.getValuesForAttribute(AttributeType.ORG);
        final List<RpslObject> orgObjects = objectDao.getByKeys(ObjectType.ORGANISATION, orgAttributes);

        return getValuesForAttribute(objectDao.getByKeys(ObjectType.ROLE, getValuesForAttribute(orgObjects, AttributeType.ABUSE_C)), AttributeType.ABUSE_MAILBOX);
    }

    private Collection<CIString> getValuesForAttribute(final Collection<RpslObject> objects, final AttributeType attributeType) {
        Set<CIString> values = Sets.newHashSet();

        for (RpslObject object : objects) {
            values.addAll(object.getValuesForAttribute(attributeType));
        }
        return values;
    }

    private boolean isMaintainedByRs(final RpslObject inetObject) {
        final Set<CIString> objectMaintainers = Sets.newHashSet();
        objectMaintainers.addAll(inetObject.getValuesForAttribute(AttributeType.MNT_BY));
        objectMaintainers.addAll(inetObject.getValuesForAttribute(AttributeType.MNT_LOWER));

        return !Sets.intersection(this.maintainers.getRsMaintainers(), objectMaintainers).isEmpty();
    }
}
