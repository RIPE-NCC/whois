package net.ripe.db.whois.rdap;

import net.ripe.db.whois.common.configuration.WhoisCommonConfiguration;
import net.ripe.db.whois.common.etree.NestedIntervalMap;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.rpsl.attrs.Domain;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(WhoisCommonConfiguration.class)
@ComponentScan(basePackages="net.ripe.db.whois.rdap")
public class RdapConfig {

    @Bean
    public NestedIntervalMap<Ipv4Resource, Domain> ipv4RipeDelegatedReverseZones() {
        final NestedIntervalMap<Ipv4Resource, Domain> tree = new NestedIntervalMap<>();
        tree.put(Ipv4Resource.parse("0/0"), Domain.parse("0.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("2.0.0.0/8"), Domain.parse("2.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("5.0.0.0/8"), Domain.parse("5.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("25.0.0.0/8"), Domain.parse("25.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("31.0.0.0/8"), Domain.parse("31.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("37.0.0.0/8"), Domain.parse("37.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("46.0.0.0/8"), Domain.parse("46.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("51.0.0.0/8"), Domain.parse("51.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("57.0.0.0/8"), Domain.parse("57.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("62.0.0.0/8"), Domain.parse("62.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("77.0.0.0/8"), Domain.parse("77.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("78.0.0.0/8"), Domain.parse("78.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("79.0.0.0/8"), Domain.parse("79.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("80.0.0.0/8"), Domain.parse("80.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("81.0.0.0/8"), Domain.parse("81.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("82.0.0.0/8"), Domain.parse("82.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("83.0.0.0/8"), Domain.parse("83.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("84.0.0.0/8"), Domain.parse("84.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("85.0.0.0/8"), Domain.parse("85.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("86.0.0.0/8"), Domain.parse("86.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("87.0.0.0/8"), Domain.parse("87.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("88.0.0.0/8"), Domain.parse("88.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("89.0.0.0/8"), Domain.parse("89.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("90.0.0.0/8"), Domain.parse("90.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("91.0.0.0/8"), Domain.parse("91.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("92.0.0.0/8"), Domain.parse("92.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("93.0.0.0/8"), Domain.parse("93.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("94.0.0.0/8"), Domain.parse("94.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("95.0.0.0/8"), Domain.parse("95.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("109.0.0.0/8"), Domain.parse("109.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("141.0.0.0/8"), Domain.parse("141.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("145.0.0.0/8"), Domain.parse("145.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("151.0.0.0/8"), Domain.parse("151.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("176.0.0.0/8"), Domain.parse("176.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("178.0.0.0/8"), Domain.parse("178.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("185.0.0.0/8"), Domain.parse("185.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("188.0.0.0/8"), Domain.parse("188.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("193.0.0.0/8"), Domain.parse("193.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("194.0.0.0/8"), Domain.parse("194.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("195.0.0.0/8"), Domain.parse("195.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("212.0.0.0/8"), Domain.parse("212.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("213.0.0.0/8"), Domain.parse("213.in-addr.arpa"));
        tree.put(Ipv4Resource.parse("217.0.0.0/8"), Domain.parse("217.in-addr.arpa"));

        return tree;
    }

    @Bean
    public NestedIntervalMap<Ipv6Resource, Domain> ipv6RipeDelegatedReverseZones() {
        final NestedIntervalMap<Ipv6Resource, Domain> tree = new NestedIntervalMap<>();

        tree.put(Ipv6Resource.parse("::/0"), Domain.parse("0.ip6.arpa"));

        //2001:600::/23
        tree.put(Ipv6Resource.parse("2001:600::/24"), Domain.parse("6.0.1.0.0.2.ip6.arpa"));
        tree.put(Ipv6Resource.parse("2001:700::/24"), Domain.parse("7.0.1.0.0.2.ip6.arpa"));

        //2001:800::/22
        tree.put(Ipv6Resource.parse("2001:800::/24"), Domain.parse("8.0.1.0.0.2.ip6.arpa"));
        tree.put(Ipv6Resource.parse("2001:900::/24"), Domain.parse("9.0.1.0.0.2.ip6.arpa"));
        tree.put(Ipv6Resource.parse("2001:a00::/24"), Domain.parse("a.0.1.0.0.2.ip6.arpa"));
        tree.put(Ipv6Resource.parse("2001:b00::/24"), Domain.parse("b.0.1.0.0.2.ip6.arpa"));

        //2001:1400::/22
        tree.put(Ipv6Resource.parse("2001:1400::/24"), Domain.parse("4.1.1.0.0.2.ip6.arpa"));
        tree.put(Ipv6Resource.parse("2001:1500::/24"), Domain.parse("5.1.1.0.0.2.ip6.arpa"));
        tree.put(Ipv6Resource.parse("2001:1600::/24"), Domain.parse("6.1.1.0.0.2.ip6.arpa"));
        tree.put(Ipv6Resource.parse("2001:1700::/24"), Domain.parse("7.1.1.0.0.2.ip6.arpa"));

        //2001:1a00::/23
        tree.put(Ipv6Resource.parse("2001:1a00::/24"), Domain.parse("a.1.1.0.0.2.ip6.arpa"));
        tree.put(Ipv6Resource.parse("2001:1b00::/24"), Domain.parse("b.1.1.0.0.2.ip6.arpa"));

        //2001:1c00::/22
        tree.put(Ipv6Resource.parse("2001:1c00::/24"), Domain.parse("c.1.1.0.0.2.ip6.arpa"));
        tree.put(Ipv6Resource.parse("2001:1d00::/24"), Domain.parse("d.1.1.0.0.2.ip6.arpa"));
        tree.put(Ipv6Resource.parse("2001:1e00::/24"), Domain.parse("e.1.1.0.0.2.ip6.arpa"));
        tree.put(Ipv6Resource.parse("2001:1f00::/24"), Domain.parse("f.1.1.0.0.2.ip6.arpa"));

        //2001:2000::/19
        tree.put(Ipv6Resource.parse("2001:2000::/20"), Domain.parse("2.1.0.0.2.ip6.arpa"));
        tree.put(Ipv6Resource.parse("2001:3000::/20"), Domain.parse("3.1.0.0.2.ip6.arpa"));

        //2001:4000::/23
        tree.put(Ipv6Resource.parse("2001:4000::/24"), Domain.parse("0.4.1.0.0.2.ip6.arpa"));
        tree.put(Ipv6Resource.parse("2001:4100::/24"), Domain.parse("1.4.1.0.0.2.ip6.arpa"));

        //2001:4600::/23
        tree.put(Ipv6Resource.parse("2001:4600::/24"), Domain.parse("6.4.1.0.0.2.ip6.arpa"));
        tree.put(Ipv6Resource.parse("2001:4700::/24"), Domain.parse("7.4.1.0.0.2.ip6.arpa"));

        //2001:4a00::/23
        tree.put(Ipv6Resource.parse("2001:4a00::/24"), Domain.parse("a.4.1.0.0.2.ip6.arpa"));
        tree.put(Ipv6Resource.parse("2001:4b00::/24"), Domain.parse("b.4.1.0.0.2.ip6.arpa"));

        //2001:4c00::/23
        tree.put(Ipv6Resource.parse("2001:4c00::/24"), Domain.parse("c.4.1.0.0.2.ip6.arpa"));
        tree.put(Ipv6Resource.parse("2001:4d00::/24"), Domain.parse("d.4.1.0.0.2.ip6.arpa"));

        //2001:5000::/20
        tree.put(Ipv6Resource.parse("2001:5000::/20"), Domain.parse("5.1.0.0.2.ip6.arpa"));

        //2003::/18
        tree.put(Ipv6Resource.parse("2003::/20"), Domain.parse("0.3.0.0.2.ip6.arpa"));
        tree.put(Ipv6Resource.parse("2003:1000::/20"), Domain.parse("1.3.0.0.2.ip6.arpa"));
        tree.put(Ipv6Resource.parse("2003:2000::/20"), Domain.parse("2.3.0.0.2.ip6.arpa"));
        tree.put(Ipv6Resource.parse("2003:3000::/20"), Domain.parse("3.3.0.0.2.ip6.arpa"));

        //2a00::/12
        tree.put(Ipv6Resource.parse("2a00::/12"), Domain.parse("0.a.2.ip6.arpa"));

        //2a10::/12
        tree.put(Ipv6Resource.parse("2a10::/12"), Domain.parse("1.a.2.ip6.arpa"));

        return tree;
    }

}
