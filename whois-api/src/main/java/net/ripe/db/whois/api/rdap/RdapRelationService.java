package net.ripe.db.whois.api.rdap;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.api.rdap.domain.Ip;
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
import net.ripe.db.whois.common.search.ManagedAttributeSearch;
import org.eclipse.jetty.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;


@Service
public class RdapRelationService {

    private final RdapQueryHandler rdapQueryHandler;
    private final Ipv4Tree ip4Tree;
    private final Ipv6Tree ip6Tree;
    private final Ipv4DomainTree ipv4DomainTree;
    private final Ipv6DomainTree ipv6DomainTree;
    private final RpslObjectDao rpslObjectDao;

    public RdapRelationService(final RdapQueryHandler rdapQueryHandler, final Ipv4Tree ip4Tree, final Ipv6Tree ip6Tree,
                               final Ipv4DomainTree ipv4DomainTree, final Ipv6DomainTree ipv6DomainTree,
                               final RpslObjectDao rpslObjectDao) {
        this.rdapQueryHandler = rdapQueryHandler;
        this.ip4Tree = ip4Tree;
        this.ip6Tree = ip6Tree;
        this.ipv4DomainTree = ipv4DomainTree;
        this.ipv6DomainTree = ipv6DomainTree;
        this.rpslObjectDao = rpslObjectDao;
    }

    public List<String> getDomainRelationPkeys(final String pkey, final RelationType relationType){
        final Domain domain = Domain.parse(pkey);
        final IpInterval reverseIp = domain.getReverseIp();

        return getRelationPkeys(relationType, getIpDomainTree(reverseIp), reverseIp);
    }

    public List<String> getInetnumRelationPkeys(final String pkey, final RelationType relationType){
        final IpInterval ip = IpInterval.parse(pkey);
        return getRelationPkeys(relationType, getIpTree(ip), ip);
    }

    private List<String> getRelationPkeys(RelationType relationType, IpTree ipTree, IpInterval ip) {
        final List<IpEntry> ipEntries = getIpEntries(ipTree, relationType, ip);
        return ipEntries.stream().map(ipEntry -> ipEntry.getKey().toString()).toList();
    }

    private List<IpEntry> getIpEntries(final IpTree ipTree, final RelationType relationType, final IpInterval reverseIp) {
        return switch (relationType) {
            case UP -> List.of(searchFirstLessSpecificCoMntner(ipTree, reverseIp));
            case TOP -> searchCoMntnerTopLevel(ipTree, reverseIp);
            case DOWN -> ipTree.findFirstMoreSpecific(reverseIp);
            case BOTTOM -> searchMostSpecificFillingOverlaps(ipTree, reverseIp);
        };
    }

    private List<IpEntry> searchMostSpecificFillingOverlaps(final IpTree ipTree, final IpInterval reverseIp){
        final List<IpEntry> mostSpecificValues = ipTree.findMostSpecific(reverseIp);
        final List<? extends IpInterval<?>> ipResources = mostSpecificValues.stream().map(ip -> IpInterval.parse(ip.getKey().toString())).toList();
        final Set<IpEntry> mostSpecificFillingOverlaps = Sets.newConcurrentHashSet();

        for (int countIps = 0; countIps < mostSpecificValues.size(); countIps++){
            final IpInterval firstResource = ipResources.get(countIps);
            processChildren(ipTree, reverseIp, firstResource, mostSpecificFillingOverlaps);
        }
        return mostSpecificFillingOverlaps.stream().toList();
    }

    private static void processChildren(final IpTree ipTree, final IpInterval reverseIp,
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
            processChildren(ipTree, reverseIp, parentInterval, mostSpecificFillingOverlaps);
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
        final IpEntry parent = (IpEntry)ipTree.findFirstLessSpecific(parentResource).getFirst();
        return ipTree.findFirstMoreSpecific(IpInterval.parse(parent.getKey().toString()));
    }

    private IpEntry searchFirstLessSpecificCoMntner(final IpTree ipTree, final IpInterval reverseIp){
        final IpEntry firstLessSpecific = (IpEntry) ipTree.findFirstLessSpecific(reverseIp).getFirst();
        final RpslObject rpslObject = rpslObjectDao.getById(firstLessSpecific.getObjectId());

        if (!isOutOfRegionOrRoot(rpslObject)) {
            return firstLessSpecific;
        }

        throw new RdapException("404 Not Found", "No up level object has been found for " + reverseIp.toString(), HttpStatus.NOT_FOUND_404);
    }


    private List<IpEntry> searchCoMntnerTopLevel(final IpTree ipTree, final IpInterval reverseIp) {
        for (final Object parentEntry : ipTree.findAllLessSpecific(reverseIp)) {
            final IpEntry ipEntry = (IpEntry) parentEntry;
            final RpslObject rpslObject = rpslObjectDao.getById(ipEntry.getObjectId());
            if (!isOutOfRegionOrRoot(rpslObject)){
                return List.of(ipEntry);
            }
        }
        throw new RdapException("404 Not Found", "No top level object has been found for " + reverseIp.toString(), HttpStatus.NOT_FOUND_404);
    }

    private static boolean isOutOfRegionOrRoot(final RpslObject rpslObject) {
        final CIString status = rpslObject.getValueForAttribute(AttributeType.STATUS);
        return (rpslObject.getType().equals(ObjectType.INETNUM) && InetnumStatus.getStatusFor(status).isOutOfRegionOrRoot())
                || (rpslObject.getType().equals(ObjectType.INET6NUM) && Inet6numStatus.getStatusFor(status).isOutOfRegionOrRoot());
    }

    private IpTree getIpTree(final IpInterval reverseIp) {
        if (reverseIp instanceof Ipv4Resource) {
            return ip4Tree;
        } else if (reverseIp instanceof Ipv6Resource) {
            return ip6Tree;
        }

        throw new IllegalArgumentException("Unexpected reverse ip: " + reverseIp);
    }

    private IpTree getIpDomainTree(final IpInterval reverseIp) {
        if (reverseIp instanceof Ipv4Resource) {
            return ipv4DomainTree;
        } else if (reverseIp instanceof Ipv6Resource) {
            return ipv6DomainTree;
        }

        throw new IllegalArgumentException("Unexpected reverse ip: " + reverseIp);
    }
}
