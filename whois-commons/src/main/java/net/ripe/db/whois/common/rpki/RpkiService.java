package net.ripe.db.whois.common.rpki;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.collect.CollectionHelper;
import net.ripe.db.whois.common.etree.NestedIntervalMap;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RpkiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpkiService.class);

    private NestedIntervalMap<Ipv4Resource, Set<Roa>> ipv4Tree;
    private NestedIntervalMap<Ipv6Resource, Set<Roa>> ipv6Tree;
    private Map<String, Set<Roa>> asnMap;

    private final RpkiDataProvider rpkiDataProvider;

    public RpkiService(final RpkiDataProvider rpkiDataProvider) {
        this.rpkiDataProvider = rpkiDataProvider;
        loadRoas();
    }

    @Scheduled(cron = "0 */15 * * * *")
    public void loadRoas() {
        final List<Roa> loadedRoas;
        try {
            loadedRoas = rpkiDataProvider.loadRoas();
        } catch (Exception ex){
            LOGGER.error("RPKI service returned an error, so the ROAs are not updated", ex);
            return;
        }

        if (loadedRoas != null && !loadedRoas.isEmpty()){
            final List<Roa> roas = loadedRoas.stream()
                    .filter(roa -> roa.getTrustAnchor() != TrustAnchor.UNSUPPORTED)
                    .collect(Collectors.toList());

            buildTrees(roas);
            LOGGER.info("downloaded {} roas from rpki", roas.size());
        }
    }

    private void buildTrees(final List<Roa> roas) {
        final NestedIntervalMap<Ipv4Resource, Set<Roa>> ipv4Tree = new NestedIntervalMap<>();
        final NestedIntervalMap<Ipv6Resource, Set<Roa>> ipv6Tree = new NestedIntervalMap<>();
        final Map<String, Set<Roa>> asnMap = Maps.newHashMap();

        for (Roa roa : roas) {
            if (isIpv4(roa.getPrefix())) {
                addRoaToTree(ipv4Tree, Ipv4Resource.parse(roa.getPrefix()), roa);
            } else {
                if (isIpv6(roa.getPrefix())) {
                    addRoaToTree(ipv6Tree, Ipv6Resource.parse(roa.getPrefix()), roa);
                }
            }
            addRoaToMap(asnMap, roa);
        }
        this.ipv4Tree = ipv4Tree;
        this.ipv6Tree = ipv6Tree;
        this.asnMap = asnMap;
    }

    private <T extends IpInterval<T>> void addRoaToTree(final NestedIntervalMap<T, Set<Roa>> tree,
                                                        final T prefix,
                                                        final Roa roa) {
        Set<Roa> roas = CollectionHelper.uniqueResult(tree.findExact(prefix));
        if (roas == null) {
            roas = Sets.newHashSet();
            tree.put(prefix, roas);
        }
        roas.add(roa);
    }

    private void addRoaToMap(final Map<String, Set<Roa>> map, final Roa roa) {
        final String asn = String.format("AS%d", roa.getAsn());
        map.computeIfAbsent( asn, value -> Sets.newHashSet()).add(roa);
    }

    private Set<Roa> getRoasFromMap(final String resource) {
        return asnMap.get(resource.toUpperCase());
    }

    public Set<Roa> findRoas(final String resource) {
        if (isIpv4(resource)) {
                return ipv4Tree.findExactAndAllLessSpecific(Ipv4Resource.parse(resource))
                    .stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
        } else {
            if (isIpv6(resource)) {
                return ipv6Tree.findExactAndAllLessSpecific(Ipv6Resource.parse(resource))
                    .stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
            } else {
                if (isASN(resource)) {
                    return getRoasFromMap(resource);
                } else {
                    throw new IllegalArgumentException("Invalid resource " + resource);
                }
            }
        }
    }

    private static boolean isIpv4(final String resource) {
        return resource.indexOf('.') > -1;
    }

    private static boolean isIpv6(final String resource) {
        return resource.indexOf(':') > -1;
    }

    private static boolean isASN(final String resource) {
        return resource.toUpperCase().startsWith("AS");
    }

}
