package net.ripe.db.whois.common.domain;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public enum Hosts {
    WHOIS1("dbc-whois1.ripe.net"),
    WHOIS2("dbc-whois2.ripe.net"),
    WHOIS3("dbc-whois3.ripe.net"),
    WHOIS4("dbc-whois4.ripe.net"),
    WHOIS5("dbc-whois5.ripe.net"),
    WHOIS6("dbc-whois6.ripe.net"),
    PRE1("dbc-pre1.ripe.net"),
    PRE2("dbc-pre2.ripe.net"),
    DEV1("dbc-dev1.ripe.net"),
    DEV2("dbc-dev2.ripe.net"),
    UNDEFINED("");

    private static final Map<Hosts, List<Hosts>> CLUSTER_MAP = Maps.newEnumMap(Hosts.class);

    static {
        addHosts(Lists.newArrayList(DEV1, DEV2));
        addHosts(Lists.newArrayList(PRE1, PRE2));
        addHosts(Lists.newArrayList(WHOIS1, WHOIS2, WHOIS3, WHOIS4));
    }

    static void addHosts(final List<Hosts> hosts) {
        for (final Hosts host : hosts) {
            CLUSTER_MAP.put(host, hosts);
        }
    }

    private final String hostName;

    private Hosts(final String hostName) {
        this.hostName = hostName;
    }

    public String getHostName() {
        return hostName;
    }

    public List<Hosts> getClusterMembers() {
        final List<Hosts> hosts = CLUSTER_MAP.get(this);
        if (hosts == null) {
            return Collections.emptyList();
        }

        return hosts;
    }

    public static Hosts getLocalHost() {
        try {
            return getHost(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            return Hosts.UNDEFINED;
        }
    }

    public static Hosts getHost(final String hostname) {
        for (final Hosts host : Hosts.values()) {
            if (host.hostName.equals(hostname)) {
                return host;
            }
        }

        return Hosts.UNDEFINED;
    }
}
