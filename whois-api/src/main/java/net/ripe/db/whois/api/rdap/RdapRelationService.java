package net.ripe.db.whois.api.rdap;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import jakarta.servlet.http.HttpServletRequest;
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
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.query.Query;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static net.ripe.db.whois.api.rdap.RdapService.COMMA_JOINER;
import static net.ripe.db.whois.common.rpsl.ObjectType.DOMAIN;
import static net.ripe.db.whois.common.rpsl.ObjectType.INET6NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.INETNUM;
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
    private final RdapRequestValidator rdapRequestValidator;
    private final RdapQueryHandler rdapQueryHandler;
    private final RdapObjectMapper rdapObjectMapper;
    private final RdapLookupService rdapLookupService;

    @Autowired
    public RdapRelationService(final Ipv4Tree ip4Tree,
                               final Ipv6Tree ip6Tree,
                               final Ipv4DomainTree ipv4DomainTree,
                               final Ipv6DomainTree ipv6DomainTree,
                               final RpslObjectDao rpslObjectDao,
                               final RdapRequestValidator rdapRequestValidator,
                               final RdapQueryHandler rdapQueryHandler,
                               final RdapObjectMapper rdapObjectMapper,
                               final RdapLookupService rdapLookupService) {
        this.ip4Tree = ip4Tree;
        this.ip6Tree = ip6Tree;
        this.ipv4DomainTree = ipv4DomainTree;
        this.ipv6DomainTree = ipv6DomainTree;
        this.rpslObjectDao = rpslObjectDao;
        this.rdapRequestValidator = rdapRequestValidator;
        this.rdapQueryHandler = rdapQueryHandler;
        this.rdapObjectMapper = rdapObjectMapper;
        this.rdapLookupService = rdapLookupService;
    }

    protected Object handleRelationQuery(final HttpServletRequest request,
                                         final Set<ObjectType> objectTypes, final RdapRequestType requestType,
                                         final RelationType relationType, final String key,
                                         final String requestUrl,
                                         final int maxResultSize) {
        final List<RpslObject> rpslObjects;
        final boolean shouldReturnLookup = relationType.equals(RelationType.UP) || relationType.equals(RelationType.TOP);
        switch (requestType) {
            case AUTNUMS -> throw new RdapException("501 Not Implemented", "Relation queries not allowed for autnum", HttpStatus.NOT_IMPLEMENTED_501);
            case DOMAINS -> {
                rdapRequestValidator.validateDomain(key);
                final List<IpEntry> domainEntries = getDomainsEntriesByRelationType(key, relationType);

                if (shouldReturnLookup){
                    final IpEntry ipEntry = domainEntries.getFirst();
                    final RpslObject domainObject = rpslObjectDao.getById(ipEntry.getObjectId());
                    final Stream<RpslObject> inetnumResult = rdapQueryHandler.handleQueryStream(getQueryObject(ImmutableSet.of(INETNUM, INET6NUM), ipEntry.getKey().toString()), request);
                    return rdapLookupService.getDomainEntity(request, Stream.of(domainObject), inetnumResult);
                }
                //TODO: [MH] This call should not be necessary, we should be able to get the reverseIp out of the IP
                final List<String> relatedPkeys = domainEntries
                        .stream()
                        .map(ipEntry -> rpslObjectDao.getById(ipEntry.getObjectId()).getKey().toString())
                        .toList();

                rpslObjects = relatedPkeys
                        .stream()
                        .flatMap(relatedPkey -> rdapQueryHandler.handleQueryStream(getQueryObject(ImmutableSet.of(DOMAIN), relatedPkey), request))
                        .toList();

            }
            case IPS -> {
                rdapRequestValidator.validateIp(request.getRequestURI(), key);
                final List<String> relatedPkeys = getInetnumRelationPkeys(key, relationType);

                if (shouldReturnLookup){
                    return rdapLookupService.lookupObject(request, objectTypes, relatedPkeys.getFirst());
                }

                rpslObjects = relatedPkeys
                        .stream()
                        .flatMap(relatedPkey -> rdapQueryHandler.handleQueryStream(getQueryObject(objectTypes, relatedPkey), request))
                        .toList();

            }
            default -> throw new RdapException("400 Bad Request", "Invalid or unknown type " + requestType.toString().toLowerCase(), HttpStatus.BAD_REQUEST_400);
        }

        return rdapObjectMapper.mapSearch(
                requestUrl,
                rpslObjects,
                maxResultSize);
    }

    private List<IpEntry> getDomainsEntriesByRelationType(final String pkey, final RelationType relationType){
        final Domain domain = Domain.parse(pkey);
        final IpInterval reverseIp = domain.getReverseIp();
        return getEntries(getIpDomainTree(reverseIp), relationType, reverseIp);
    }

    private List<String> getInetnumRelationPkeys(final String pkey, final RelationType relationType){
        final IpInterval ip = IpInterval.parse(pkey);
        final List<IpEntry> ipEntries = getEntries(getIpTree(ip), relationType, ip);
        return ipEntries
                .stream()
                .map(ipEntry -> ipEntry.getKey().toString())
                .toList();
    }

    private List<IpEntry> getEntries(final IpTree ipTree, final RelationType relationType,
                                     final IpInterval searchIp) {
        return switch (relationType) {
            case UP -> List.of(searchUpResource(ipTree, searchIp));
            case TOP -> List.of(searchTopLevelResource(ipTree, searchIp));
            case DOWN -> ipTree.findFirstMoreSpecific(searchIp);
            case BOTTOM -> searchBottomResources(ipTree, searchIp);
            default -> throw new RdapException("400 Bad Request", "Unsupported Relation Type", HttpStatus.BAD_REQUEST_400);
        };
    }

    private List<IpEntry> searchBottomResources(final IpTree ipTree, final IpInterval searchIp){
        final List<IpEntry> mostSpecificValues = ipTree.findMostSpecific(searchIp);
        final Set<IpEntry> mostSpecificFillingOverlaps = Sets.newHashSet();
        mostSpecificValues.forEach(ipResource -> extractBottomMatches(ipTree, searchIp, ipResource, mostSpecificFillingOverlaps));
        return mostSpecificFillingOverlaps.stream().toList();
    }

    private void extractBottomMatches(final IpTree ipTree, final IpInterval searchIp,
                                      final IpEntry mostSpecificResource,
                                      final Set<IpEntry> mostSpecificFillingOverlaps) {

        mostSpecificFillingOverlaps.add(mostSpecificResource);

        final IpInterval mostSpecificInterval = IpInterval.parse(mostSpecificResource.getKey().toString());
        final List<IpEntry> parentList = ipTree.findFirstLessSpecific(mostSpecificInterval);

        if (parentList.isEmpty()){
            return;
        }

        final List<IpEntry> siblingsAndExact = findSiblingsAndExact(ipTree, parentList);

        final IpInterval firstSibling = (IpInterval)siblingsAndExact.getFirst().getKey();
        final IpInterval lastSibling = (IpInterval)siblingsAndExact.getLast().getKey();

        final IpEntry parent = parentList.getFirst();
        final IpInterval parentInterval = (IpInterval) parent.getKey();

        if (!parentInterval.equals(searchIp) && // If the parent is already the search ip we stop
                !childrenCoverParentRange(firstSibling, lastSibling, parentInterval)){
            extractBottomMatches(ipTree, searchIp, parent, mostSpecificFillingOverlaps);
        }
    }

    private boolean childrenCoverParentRange(final IpInterval firstResource, final IpInterval lastResource, final IpInterval parent){
        return firstResource.beginAsInetAddress().equals(parent.beginAsInetAddress()) &&
                lastResource.endAsInetAddress().equals(parent.endAsInetAddress());
    }

    private List<IpEntry> findSiblingsAndExact(final IpTree ipTree, final List<IpEntry> parent) {
        return ipTree.findFirstMoreSpecific(IpInterval.parse(parent.getFirst().getKey().toString()));
    }

    private IpEntry searchUpResource(final IpTree ipTree, final IpInterval searchIp){
        final List<IpEntry> parentList = ipTree.findFirstLessSpecific(searchIp);
        if (parentList.isEmpty() || !existAndNoAdministrative(searchIp, parentList.getFirst())){
            throw new RdapException("404 Not Found", "No up level object has been found for " + searchIp.toString(), HttpStatus.NOT_FOUND_404);
        }
        return parentList.getFirst();
    }

    private IpEntry searchTopLevelResource(final IpTree ipTree, final IpInterval searchIp){
        final List<IpEntry> lessAndExact = ipTree.findExactAndAllLessSpecific(searchIp);;

        for (int countLessSpecific = 0; countLessSpecific < lessAndExact.size() - 1; countLessSpecific++){
            final IpEntry ipEntry = lessAndExact.get(countLessSpecific);

            final IpInterval childIpInterval = (IpInterval)lessAndExact.get(countLessSpecific+1).getKey();
            if (existAndNoAdministrative(childIpInterval, ipEntry)){
                return ipEntry;
            }
        }
        throw new RdapException("404 Not Found", "No top-level object has been found for " + searchIp.toString(), HttpStatus.NOT_FOUND_404);
    }

    private boolean existAndNoAdministrative(final IpInterval searchIp, final IpEntry firstLessSpecific){
        final RpslObject child = getResourceByKey(searchIp);
        final RpslObject rpslObject = getResourceByKey((IpInterval) firstLessSpecific.getKey());
        if (child == null || rpslObject == null) {
            LOGGER.debug("INET(6)NUM {} does not exist in RIPE Database ", firstLessSpecific.getKey().toString());
            return false;
        }
        return !isAdministrativeResource(child, rpslObject);
    }

    private boolean isAdministrativeResource(final RpslObject child, final RpslObject rpslObject) {
        final CIString childStatus = child.getValueForAttribute(AttributeType.STATUS);
        final CIString statusAttributeValue = rpslObject.getValueForAttribute(AttributeType.STATUS);
        return (rpslObject.getType() == ObjectType.INETNUM && InetnumStatus.getStatusFor(statusAttributeValue) == ALLOCATED_UNSPECIFIED)
                || (rpslObject.getType() == ObjectType.INET6NUM) &&
                Inet6numStatus.getStatusFor(childStatus) == ALLOCATED_BY_RIR && Inet6numStatus.getStatusFor(statusAttributeValue) == ALLOCATED_BY_RIR;
    }


    @Nullable
    private RpslObject getResourceByKey(final IpInterval keyInterval){
        return rpslObjectDao.getByKeyOrNull(keyInterval instanceof Ipv4Resource ? ObjectType.INETNUM : ObjectType.INET6NUM, keyInterval.toString());
    }

    private IpTree getIpTree(final IpInterval searchIp) {
        return searchIp instanceof Ipv4Resource ? ip4Tree : ip6Tree;
    }

    private IpTree getIpDomainTree(final IpInterval reverseIp) {
        return reverseIp instanceof Ipv4Resource ? ipv4DomainTree : ipv6DomainTree;
    }


    private Query getQueryObject(final Set<ObjectType> objectTypes, final String key) {
        return Query.parse(
                String.format("%s %s %s %s %s %s",
                        QueryFlag.NO_GROUPING.getLongFlag(),
                        QueryFlag.NO_REFERENCED.getLongFlag(),
                        QueryFlag.SELECT_TYPES.getLongFlag(),
                        objectTypesToString(objectTypes),
                        QueryFlag.NO_FILTERING.getLongFlag(),
                        key));
    }

    private String objectTypesToString(final Collection<ObjectType> objectTypes) {
        return COMMA_JOINER.join(objectTypes.stream().map(ObjectType::getName).toList());
    }

}
