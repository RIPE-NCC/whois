package net.ripe.db.whois.api.rdap;

import com.google.common.collect.Sets;
import net.ripe.db.whois.api.rdap.domain.RelationType;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.iptree.IpEntry;
import net.ripe.db.whois.common.iptree.IpTree;
import net.ripe.db.whois.common.iptree.Ipv4DomainTree;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.iptree.Ipv6DomainTree;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.Domain;
import net.ripe.db.whois.common.rpsl.attrs.Inet6numStatus;
import net.ripe.db.whois.common.rpsl.attrs.InetnumStatus;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static net.ripe.db.whois.common.rpsl.attrs.Inet6numStatus.ALLOCATED_BY_RIR;
import static net.ripe.db.whois.common.rpsl.attrs.InetnumStatus.ALLOCATED_UNSPECIFIED;


@Service
public class RdapRelationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RdapRelationService.class);

    private final Ipv4Tree ip4Tree;
    private final Ipv6Tree ip6Tree;
    private final Ipv4DomainTree ipv4DomainTree;
    private final Ipv6DomainTree ipv6DomainTree;
    private final RpslObjectDao rpslObjectDao;

    @Autowired
    public RdapRelationService(final Ipv4Tree ip4Tree, final Ipv6Tree ip6Tree,
                               final Ipv4DomainTree ipv4DomainTree, final Ipv6DomainTree ipv6DomainTree,
                               final RpslObjectDao rpslObjectDao) {
        this.ip4Tree = ip4Tree;
        this.ip6Tree = ip6Tree;
        this.ipv4DomainTree = ipv4DomainTree;
        this.ipv6DomainTree = ipv6DomainTree;
        this.rpslObjectDao = rpslObjectDao;
    }

    public List<String> getDomainRelationPkeys(final String pkey, final RelationType relationType){
        final Domain domain = Domain.parse(pkey);
        final IpInterval reverseIp = domain.getReverseIp();
        final List<IpEntry> ipEntries = getIpEntries(getIpDomainTree(reverseIp), relationType, reverseIp);

        return ipEntries
                .stream()
                .map(ipEntry -> rpslObjectDao.getById(ipEntry.getObjectId()).getKey().toString())
                .toList();
    }

    public List<String> getInetnumRelationPkeys(final String pkey, final RelationType relationType){
        final IpInterval ip = IpInterval.parse(pkey);
        final List<IpEntry> ipEntries = getIpEntries(getIpTree(ip), relationType, ip);
        return ipEntries
                .stream()
                .map(ipEntry -> ipEntry.getKey().toString())
                .toList();
    }

    private List<IpEntry> getIpEntries(final IpTree ipTree, final RelationType relationType,
                                       final IpInterval reverseIp) {
        return switch (relationType) {
            case UP -> List.of(searchFirstLessSpecific(ipTree, reverseIp));
            case TOP -> searchTopLevelResource(ipTree, reverseIp);
            case DOWN -> ipTree.findFirstMoreSpecific(reverseIp);
            case BOTTOM -> searchMostSpecificFillingOverlaps(ipTree, reverseIp);
        };
    }

    private List<IpEntry> searchMostSpecificFillingOverlaps(final IpTree ipTree, final IpInterval reverseIp){
        final List<IpEntry> mostSpecificValues = ipTree.findMostSpecific(reverseIp);
        final List<? extends IpInterval<?>> ipResources = mostSpecificValues.stream().map(ip -> IpInterval.parse(ip.getKey().toString())).toList();
        final Set<IpEntry> mostSpecificFillingOverlaps = Sets.newConcurrentHashSet();
        ipResources.forEach(ipResource -> extractBottomMatches(ipTree, reverseIp, ipResource, mostSpecificFillingOverlaps));
        return mostSpecificFillingOverlaps.stream().toList();
    }

    private void extractBottomMatches(final IpTree ipTree, final IpInterval reverseIp,
                                      final IpInterval mostSpecificResource,
                                      final Set<IpEntry> mostSpecificFillingOverlaps) {

        final List<IpEntry> siblingsAndExact = findSiblingsAndExact(ipTree, mostSpecificResource);

        final IpEntry firstResourceIpEntry = siblingsAndExact.stream()
                .filter(sibling -> sibling.getKey().toString().equals(mostSpecificResource.toString()))
                .findFirst().orElse(null);

        mostSpecificFillingOverlaps.add(firstResourceIpEntry);

        final IpInterval firstSibling = (IpInterval)siblingsAndExact.getFirst().getKey();
        final IpInterval lastSibling = (IpInterval)siblingsAndExact.getLast().getKey();

        final IpEntry parent = (IpEntry) ipTree.findFirstLessSpecific(IpInterval.parse(mostSpecificResource.toString())).getFirst();
        final IpInterval parentInterval = (IpInterval) parent.getKey();

        if (!parentInterval.equals(reverseIp) &&
                !childrenCoverParentRange(firstSibling, lastSibling, parentInterval)){
            extractBottomMatches(ipTree, reverseIp, parentInterval, mostSpecificFillingOverlaps);
        }
    }

    private static boolean childrenCoverParentRange(final IpInterval firstResource, final IpInterval lastResource, final IpInterval parent){
        if (firstResource instanceof Ipv4Resource ipv4Resource){
            final Ipv4Resource lastIpv4Resource = (Ipv4Resource)lastResource;
            final Ipv4Resource parentIpv4Resource = (Ipv4Resource)parent;
            return ipv4Resource.begin() == parentIpv4Resource.begin() && lastIpv4Resource.end() == parentIpv4Resource.end();
        }

        final Ipv6Resource ipv6Resource = (Ipv6Resource)firstResource;
        final Ipv6Resource lastIpv6Resource = (Ipv6Resource)lastResource;
        final Ipv6Resource parentIpv6Resource = (Ipv6Resource)parent;
        return Objects.equals(ipv6Resource.begin(), parentIpv6Resource.begin()) && Objects.equals(lastIpv6Resource.end(), parentIpv6Resource.end());
    }

    private static List<IpEntry> findSiblingsAndExact(final IpTree ipTree, final IpInterval parentResource) {
        final List<IpEntry> parentList = ipTree.findFirstLessSpecific(parentResource);
        if (parentList.isEmpty()){
            return ipTree.findExact(parentResource);
        }
        return ipTree.findFirstMoreSpecific(IpInterval.parse(parentList.getFirst().getKey().toString()));
    }

    private IpEntry searchFirstLessSpecific(final IpTree ipTree, final IpInterval reverseIp){
        final List<IpEntry> parentList = ipTree.findFirstLessSpecific(reverseIp);
        if (parentList.isEmpty() || !resourceExist(reverseIp, parentList.getFirst())){
            throw new RdapException("404 Not Found", "No up level object has been found for " + reverseIp.toString(), HttpStatus.NOT_FOUND_404);
        }
        return parentList.getFirst();
    }

    private List<IpEntry> searchTopLevelResource(final IpTree ipTree, final IpInterval reverseIp){
        IpEntry ipEntry;
        try {
            ipEntry = searchFirstLessSpecific(ipTree, reverseIp);
        } catch (RdapException ex){
            throw new RdapException("404 Not Found", "No top-level object has been found for " + reverseIp.toString(), HttpStatus.NOT_FOUND_404);
        }

        return List.of(loopUpLevels(ipTree, ipEntry));
    }

    private IpEntry loopUpLevels(final IpTree ipTree, IpEntry reverseIp) {
        try {
            reverseIp = searchFirstLessSpecific(ipTree, (IpInterval) reverseIp.getKey());
            loopUpLevels(ipTree, reverseIp);
        } catch (RdapException ex){
            /*
            * Do Nothing, end of loop
            * */
        }
        return reverseIp;
    }

    private boolean resourceExist(final IpInterval ip, final IpEntry firstLessSpecific){
        final RpslObject children = getResourceByKey(ip.toString());
        final RpslObject rpslObject = getResourceByKey(firstLessSpecific.getKey().toString());
        if (rpslObject == null || isAdministrativeResource(children, rpslObject)) {
            LOGGER.debug("INET(6)NUM {} does not exist in RIPE Database ", firstLessSpecific.getKey().toString());
            return false;
        }
        return true;
    }

    private static boolean isAdministrativeResource(final RpslObject children, final RpslObject rpslObject) {
        final CIString childrenStatus = children.getValueForAttribute(AttributeType.STATUS);
        final CIString statusAttributeValue = rpslObject.getValueForAttribute(AttributeType.STATUS);
        return (rpslObject.getType().equals(ObjectType.INETNUM) && InetnumStatus.getStatusFor(statusAttributeValue).equals(ALLOCATED_UNSPECIFIED))
                || (rpslObject.getType().equals(ObjectType.INET6NUM) && !Inet6numStatus.getStatusFor(childrenStatus).isAssignment() &&
                Inet6numStatus.getStatusFor(statusAttributeValue).equals(ALLOCATED_BY_RIR));
    }


    @Nullable
    private RpslObject getResourceByKey(final String key){
        if (IpInterval.parse(key) instanceof Ipv4Resource){
            return rpslObjectDao.getByKeyOrNull(ObjectType.INETNUM, key);
        }
        return rpslObjectDao.getByKeyOrNull(ObjectType.INET6NUM, key);
    }

    private IpTree getIpTree(final IpInterval reverseIp) {
        if (reverseIp instanceof Ipv4Resource) {
            return ip4Tree;
        }
        return ip6Tree;
    }

    private IpTree getIpDomainTree(final IpInterval reverseIp) {
        if (reverseIp instanceof Ipv4Resource) {
            return ipv4DomainTree;
        }
        return ipv6DomainTree;
    }
}
