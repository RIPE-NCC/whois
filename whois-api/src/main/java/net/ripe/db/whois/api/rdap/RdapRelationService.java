package net.ripe.db.whois.api.rdap;

import com.google.common.collect.Lists;
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
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.Domain;
import net.ripe.db.whois.common.search.ManagedAttributeSearch;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.eclipse.jetty.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class RdapRelationService {

    private final RdapQueryHandler rdapQueryHandler;
    private final Ipv4Tree ip4Tree;
    private final Ipv6Tree ip6Tree;
    private final Ipv4DomainTree ipv4DomainTree;
    private final Ipv6DomainTree ipv6DomainTree;
    private final RpslObjectDao rpslObjectDao;
    private final ManagedAttributeSearch managedAttributeSearch;

    public RdapRelationService(final RdapQueryHandler rdapQueryHandler, final Ipv4Tree ip4Tree, final Ipv6Tree ip6Tree,
                               final Ipv4DomainTree ipv4DomainTree, final Ipv6DomainTree ipv6DomainTree,
                               final RpslObjectDao rpslObjectDao,
                               final ManagedAttributeSearch managedAttributeSearch) {
        this.rdapQueryHandler = rdapQueryHandler;
        this.ip4Tree = ip4Tree;
        this.ip6Tree = ip6Tree;
        this.ipv4DomainTree = ipv4DomainTree;
        this.ipv6DomainTree = ipv6DomainTree;
        this.rpslObjectDao = rpslObjectDao;
        this.managedAttributeSearch = managedAttributeSearch;
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
            case UP -> ipTree.findFirstLessSpecific(reverseIp);
            case TOP -> searchCoMntnerTopLevel(ipTree, reverseIp);
            case DOWN -> ipTree.findFirstMoreSpecific(reverseIp);
            case BOTTOM -> ipTree.findMostSpecific(reverseIp);
        };
    }

    private List<IpEntry> searchCoMntnerTopLevel(final IpTree ipTree, final IpInterval reverseIp) {
        for (final Object parentEntry : ipTree.findAllLessSpecific(reverseIp)) {
            final IpEntry ipEntry = (IpEntry) parentEntry;
            final RpslObject rpslObject = rpslObjectDao.getById(ipEntry.getObjectId());
            if (managedAttributeSearch.isCoMaintained(rpslObject)){
                return List.of(ipEntry);
            }
        }
        throw new RdapException("404 Not Found", "No top level object has been found for " + reverseIp.toString(), HttpStatus.NOT_FOUND_404);
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
