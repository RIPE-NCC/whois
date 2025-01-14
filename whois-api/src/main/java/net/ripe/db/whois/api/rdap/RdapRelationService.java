package net.ripe.db.whois.api.rdap;

import com.google.common.collect.Sets;
import net.ripe.db.whois.api.rdap.domain.Autnum;
import net.ripe.db.whois.api.rdap.domain.Ip;
import net.ripe.db.whois.api.rdap.domain.Link;
import net.ripe.db.whois.api.rdap.domain.RdapObject;
import net.ripe.db.whois.api.rdap.domain.RdapRequestType;
import net.ripe.db.whois.api.rdap.domain.RelationType;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.List;
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
    private final String rdapRirSearchSkeleton;

    @Autowired
    public RdapRelationService(final Ipv4Tree ip4Tree, final Ipv6Tree ip6Tree,
                               final Ipv4DomainTree ipv4DomainTree, final Ipv6DomainTree ipv6DomainTree,
                               final RpslObjectDao rpslObjectDao,
                               @Value("${rdap.public.baseUrl:}") final String baseUrl) {
        this.ip4Tree = ip4Tree;
        this.ip6Tree = ip6Tree;
        this.ipv4DomainTree = ipv4DomainTree;
        this.ipv6DomainTree = ipv6DomainTree;
        this.rpslObjectDao = rpslObjectDao;
        this.rdapRirSearchSkeleton = baseUrl + "/%s/rirSearch1/%s/%s";
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

    public void mapRelationLinks(final RdapObject rdapResponse, final String requestUrl){
        if (requestUrl == null || requestUrl.contains("rirSearch1")){
            return;
        }
        switch (rdapResponse){
            case Ip ip -> mapCommonRelationLinks(rdapResponse, requestUrl, "ips", ip.getHandle());
            case net.ripe.db.whois.api.rdap.domain.Domain domain -> mapCommonRelationLinks(rdapResponse, requestUrl, "domains", domain.getHandle());
            case Autnum autnum -> mapCommonRelationLinks(rdapResponse, requestUrl, "autnums", autnum.getHandle());
            default -> {}
        }
    }

    private void mapCommonRelationLinks(final RdapObject rdapResponse, final String requestUrl, final String objectType, final String handle){
        rdapResponse.getLinks().add(new Link(requestUrl, RelationType.UP.getValue(), String.format(rdapRirSearchSkeleton,
                objectType, RelationType.UP.getValue(), handle), "application/rdap+json", null, null));

        rdapResponse.getLinks().add(new Link(requestUrl, "up-active", String.format(rdapRirSearchSkeleton + "?status=active",
                objectType, RelationType.UP.getValue(), handle), "application/rdap+json", null, null));

        rdapResponse.getLinks().add(new Link(requestUrl, RelationType.DOWN.getValue(), String.format(rdapRirSearchSkeleton,
                objectType, RelationType.DOWN.getValue(), handle), "application/rdap+json", null, null));

        rdapResponse.getLinks().add(new Link(requestUrl, RelationType.TOP.getValue(), String.format(rdapRirSearchSkeleton,
                objectType, RelationType.TOP.getValue(), handle), "application/rdap+json", null, null));

        rdapResponse.getLinks().add(new Link(requestUrl, "top-active", String.format(rdapRirSearchSkeleton + "?status=active",
                objectType, RelationType.TOP.getValue(), handle), "application/rdap+json", null, null));

        rdapResponse.getLinks().add(new Link(requestUrl, RelationType.BOTTOM.getValue(), String.format(rdapRirSearchSkeleton,
                objectType, RelationType.BOTTOM.getValue(), handle), "application/rdap+json", null, null));
    }

    public void includeRirSearchConformance(final RdapObject rdapObject, final String requestUrl){
        rdapObject.getRdapConformance().add(RdapConformance.RIR_SEARCH_1.getValue());
        if (requestUrl == null) {
            return;
        }
        if (requestUrl.contains(RdapRequestType.IPS.name().toLowerCase())){
            rdapObject.getRdapConformance().addAll(List.of(RdapConformance.IPS.getValue(), RdapConformance.IP_SEARCH_RESULTS.getValue()));
            return;
        }
        if (requestUrl.contains(RdapRequestType.AUTNUMS.name().toLowerCase())){
            rdapObject.getRdapConformance().addAll(List.of(RdapConformance.AUTNUMS.getValue(), RdapConformance.AUTNUM_SEARCH_RESULTS.getValue()));
        }
    }

    private List<IpEntry> getIpEntries(final IpTree ipTree, final RelationType relationType,
                                       final IpInterval searchIp) {
        return switch (relationType) {
            case UP -> List.of(searchFirstLessSpecific(ipTree, searchIp));
            case TOP -> List.of(searchTopLevelResource(ipTree, searchIp));
            case DOWN -> ipTree.findFirstMoreSpecific(searchIp);
            case BOTTOM -> searchBottomResources(ipTree, searchIp);
        };
    }

    private List<IpEntry> searchBottomResources(final IpTree ipTree, final IpInterval searchIp){
        final List<IpEntry> mostSpecificValues = ipTree.findMostSpecific(searchIp);
        final Set<IpEntry> mostSpecificFillingOverlaps = Sets.newConcurrentHashSet();
        mostSpecificValues.forEach(ipResource -> extractBottomMatches(ipTree, searchIp, ipResource, mostSpecificFillingOverlaps));
        return mostSpecificFillingOverlaps.stream().toList();
    }

    private void extractBottomMatches(final IpTree ipTree, final IpInterval searchIp,
                                      final IpEntry mostSpecificResource,
                                      final Set<IpEntry> mostSpecificFillingOverlaps) {
        mostSpecificFillingOverlaps.add(mostSpecificResource);

        final IpInterval mostSpecificInterval = IpInterval.parse(mostSpecificResource.getKey().toString());
        final List<IpEntry> parentList = ipTree.findFirstLessSpecific(mostSpecificInterval);
        final List<IpEntry> siblingsAndExact = findSiblingsAndExact(ipTree, mostSpecificInterval, parentList);

        final IpInterval firstSibling = (IpInterval)siblingsAndExact.getFirst().getKey();
        final IpInterval lastSibling = (IpInterval)siblingsAndExact.getLast().getKey();

        final IpEntry parent = parentList.getFirst();
        final IpInterval parentInterval = (IpInterval) parent.getKey();

        if (!parentInterval.equals(searchIp) && // If the parent is already the search ip we stop
                !childrenCoverParentRange(firstSibling, lastSibling, parentInterval)){
            extractBottomMatches(ipTree, searchIp, parent, mostSpecificFillingOverlaps);
        }
    }

    private static boolean childrenCoverParentRange(final IpInterval firstResource, final IpInterval lastResource, final IpInterval parent){
        return firstResource.beginAsInetAddress().equals(parent.beginAsInetAddress()) &&
                lastResource.endAsInetAddress().equals(parent.endAsInetAddress());
    }

    private static List<IpEntry> findSiblingsAndExact(final IpTree ipTree, final IpInterval parentResource, final List<IpEntry> parent) {
        if (parent.isEmpty()){
            return ipTree.findExact(parentResource);
        }
        return ipTree.findFirstMoreSpecific(IpInterval.parse(parent.getFirst().getKey().toString()));
    }

    private IpEntry searchFirstLessSpecific(final IpTree ipTree, final IpInterval searchIp){
        final List<IpEntry> parentList = ipTree.findFirstLessSpecific(searchIp);
        if (parentList.isEmpty() || !resourceExist(searchIp, parentList.getFirst())){
            throw new RdapException("404 Not Found", "No up level object has been found for " + searchIp.toString(), HttpStatus.NOT_FOUND_404);
        }
        return parentList.getFirst();
    }

    private IpEntry searchTopLevelResource(final IpTree ipTree, final IpInterval searchIp){
        IpEntry ipEntry;
        try {
            ipEntry = searchFirstLessSpecific(ipTree, searchIp);
        } catch (RdapException ex){
            throw new RdapException("404 Not Found", "No top-level object has been found for " + searchIp.toString(), HttpStatus.NOT_FOUND_404);
        }

        return loopUpLevels(ipTree, ipEntry);
    }

    private IpEntry loopUpLevels(final IpTree ipTree, IpEntry searchIp) {
        try {
            searchIp = searchFirstLessSpecific(ipTree, (IpInterval) searchIp.getKey());
            loopUpLevels(ipTree, searchIp);
        } catch (RdapException ex){
            /*
            * Do Nothing, end of loop
            * */
        }
        return searchIp;
    }

    private boolean resourceExist(final IpInterval searchIp, final IpEntry firstLessSpecific){
        final RpslObject children = getResourceByKey(searchIp.toString());
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
                || (rpslObject.getType().equals(ObjectType.INET6NUM) && Inet6numStatus.getStatusFor(childrenStatus).equals(ALLOCATED_BY_RIR) &&
                Inet6numStatus.getStatusFor(statusAttributeValue).equals(ALLOCATED_BY_RIR));
    }


    @Nullable
    private RpslObject getResourceByKey(final String key){
        if (IpInterval.parse(key) instanceof Ipv4Resource){
            return rpslObjectDao.getByKeyOrNull(ObjectType.INETNUM, key);
        }
        return rpslObjectDao.getByKeyOrNull(ObjectType.INET6NUM, key);
    }

    private IpTree getIpTree(final IpInterval searchIp) {
        if (searchIp instanceof Ipv4Resource) {
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
