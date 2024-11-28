package net.ripe.db.whois.api.rdap;

import net.ripe.db.whois.api.rdap.domain.RelationType;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.iptree.IpEntry;
import net.ripe.db.whois.common.iptree.IpTree;
import net.ripe.db.whois.common.iptree.Ipv4DomainTree;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.iptree.Ipv6DomainTree;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.attrs.Domain;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RdapRelationService {

    private final RdapQueryHandler rdapQueryHandler;
    private final Ipv4Tree ip4Tree;
    private final Ipv6Tree ip6Tree;
    private final Ipv4DomainTree ipv4DomainTree;
    private final Ipv6DomainTree ipv6DomainTree;

    public RdapRelationService(final RdapQueryHandler rdapQueryHandler, final Ipv4Tree ip4Tree, final Ipv6Tree ip6Tree,
                               final Ipv4DomainTree ipv4DomainTree, final Ipv6DomainTree ipv6DomainTree) {
        this.rdapQueryHandler = rdapQueryHandler;
        this.ip4Tree = ip4Tree;
        this.ip6Tree = ip6Tree;
        this.ipv4DomainTree = ipv4DomainTree;
        this.ipv6DomainTree = ipv6DomainTree;
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
            case TOP -> List.of((IpEntry) ipTree.findAllLessSpecific(reverseIp).getFirst());
            case DOWN -> ipTree.findFirstMoreSpecific(reverseIp);
            case BOTTOM -> ipTree.findAllMoreSpecific(reverseIp); //TODO: [MH] get the MOST specific, can be more than 1
        };
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
