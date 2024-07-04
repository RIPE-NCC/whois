package net.ripe.db.whois.common.rpki;

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
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RpkiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpkiService.class);

    private NestedIntervalMap<Ipv4Resource, Set<Roa>> ipv4Tree;
    private NestedIntervalMap<Ipv6Resource, Set<Roa>> ipv6Tree;

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
        for (Roa roa : roas) {
            if (isIpv4(roa.getPrefix())) {
                addRoaToTree(ipv4Tree, Ipv4Resource.parse(roa.getPrefix()), roa);
            } else {
                addRoaToTree(ipv6Tree, Ipv6Resource.parse(roa.getPrefix()), roa);
            }
        }
        this.ipv4Tree = ipv4Tree;
        this.ipv6Tree = ipv6Tree;
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

    public Set<Roa> findRoas(final String prefix) {
        return (isIpv4(prefix)?
                ipv4Tree.findExactAndAllLessSpecific(Ipv4Resource.parse(prefix)) :
                ipv6Tree.findExactAndAllLessSpecific(Ipv6Resource.parse(prefix)))
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    private static boolean isIpv4(final String prefix) {
        return prefix.contains(".");
    }
}
