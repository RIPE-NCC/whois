package net.ripe.db.whois.query.planner;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.collect.CollectionHelper;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
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

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

@Component
public class AbuseCFinder {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbuseCFinder.class);

    private final RpslObjectDao objectDao;
    private final Ipv4Tree ipv4Tree;
    private final Ipv6Tree ipv6Tree;
    private final Maintainers maintainers;

    @Autowired
    public AbuseCFinder(final RpslObjectDao objectDao,
                        final Ipv4Tree ipv4Tree,
                        final Ipv6Tree ipv6Tree,
                        final Maintainers maintainers) {
        this.objectDao = objectDao;
        this.ipv4Tree = ipv4Tree;
        this.ipv6Tree = ipv6Tree;
        this.maintainers = maintainers;
    }

    @CheckForNull
    @Nullable
    public String getAbuseContact(final RpslObject object) {
        String abuseContact = getAbuseMailbox(object);

        if ((abuseContact == null) && (object.getType() != ObjectType.AUT_NUM)) {
            RpslObject parentObject = object;

            while (abuseContact == null) {
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

                abuseContact = getAbuseMailbox(parentObject);

                if (isMaintainedByRs(parentObject)) {
                    break;
                }
            }
        }

        return abuseContact;
    }

    @Nullable
    private String getAbuseMailbox(final RpslObject object) {
        try {
            if (object.containsAttribute(AttributeType.ORG)) {
                final RpslObject organisation = objectDao.getByKey(ObjectType.ORGANISATION, object.getValueForAttribute(AttributeType.ORG));
                if (organisation.containsAttribute(AttributeType.ABUSE_C)) {
                    final RpslObject abuseCRole = objectDao.getByKey(ObjectType.ROLE, organisation.getValueForAttribute(AttributeType.ABUSE_C));
                    if (abuseCRole.containsAttribute(AttributeType.ABUSE_MAILBOX)) {
                        return abuseCRole.getValueForAttribute(AttributeType.ABUSE_MAILBOX).toString();
                    }
                }
            }
        } catch (EmptyResultDataAccessException ignored) {
            LOGGER.debug("Ignored invalid reference (object {})", object.getKey());
        }

        return null;
    }

    private boolean isMaintainedByRs(final RpslObject inetObject) {
        final Set<CIString> objectMaintainers = Sets.newHashSet();
        objectMaintainers.addAll(inetObject.getValuesForAttribute(AttributeType.MNT_BY));
        objectMaintainers.addAll(inetObject.getValuesForAttribute(AttributeType.MNT_LOWER));

        return !Sets.intersection(this.maintainers.getRsMaintainers(), objectMaintainers).isEmpty();
    }
}
