package net.ripe.db.whois.api.httpserver.hazelcast;

import com.google.common.base.Joiner;
import com.hazelcast.collection.ISet;
import com.hazelcast.core.HazelcastInstance;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.annotation.ManagedOperation;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public class WhoisHazelcastManagerFilter implements WhoisHazelcastManagerFilterMBean, Filter {
    private static final Logger LOGGER = getLogger(WhoisHazelcastManagerFilter.class);

    private static final Joiner COMMA_JOINER = Joiner.on(',');

    private final ISet<Ipv4Resource> ipv4blockedSet;
    private final ISet<Ipv6Resource> ipv6blockedSet;

    public WhoisHazelcastManagerFilter(HazelcastInstance hazelcastInstance, final String commaSeparatedList) {
        ipv4blockedSet = hazelcastInstance.getSet("ipv4blockedSet");
        ipv6blockedSet = hazelcastInstance.getSet("ipv6blockedSet");

        for (final String address : StringUtil.csvSplit(commaSeparatedList)) {
            addBlockedListAddress(address);
        }
    }

    @Override
    @ManagedOperation("adds an IP address to blocked list")
    public String addBlockedListAddress(String address) {
        if (address.contains(".")) {
            ipv4blockedSet.add(Ipv4Resource.parse(address));
        } else {
            ipv6blockedSet.add(Ipv6Resource.parse(address));
        }
        LOGGER.info("Ipaddress {} added to blocked list", address);
        return String.format("Ipaddress %s added to blocked list", address);
    }

    @Override
    @ManagedOperation("Remove an IP address to blocked list")
    public String removeBlockedListAddress(String address) {
        if (address.contains(".")){
            ipv4blockedSet.remove(Ipv4Resource.parse(address));
        } else {
            ipv6blockedSet.remove(Ipv6Resource.parse(address));
        }
        LOGGER.info("Ipaddress {} removed from blocked list", address);
        return String.format("Ipaddress %s removed from blocked list", address);
    }

    @Override
    @ManagedOperation("Retrieve blocked list")
    public String getBlockedList() {
        StringBuilder result = new StringBuilder();
        COMMA_JOINER.appendTo(result, ipv4blockedSet);

        if (!ipv6blockedSet.isEmpty()){
            result.append(',');
            COMMA_JOINER.appendTo(result, ipv6blockedSet);
        }

        LOGGER.info("The blocked list contains next IPs {}", result);
        return String.format("The blocked list contains next IPs %s", result);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        final HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        if (isBlockedIp(httpRequest.getRemoteAddr())){
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS_429);
            httpResponse.getWriter().write("You have been permanently blocked. Please contact support");
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        ipv4blockedSet.clear();
        ipv6blockedSet.clear();
        LOGGER.info("Blocked listed IPs have been removed");
    }

    private boolean isBlockedIp(final String candidate) {
        // TODO: Duplicated in WhoisDoSFilter, maybe put this into a utils or into a interface with a default method
        final IpInterval<?> parsed = IpInterval.parse(candidate);
        return switch (parsed) {
            case Ipv4Resource ipv4Resource -> {
                for (Ipv4Resource entry : ipv4blockedSet) {
                    if (entry.contains(ipv4Resource)) {
                        yield true;
                    }
                }
                yield false;
            }
            case Ipv6Resource ipv6Resource -> {
                for (Ipv6Resource entry : ipv6blockedSet) {
                    if (entry.contains(ipv6Resource)) {
                        yield true;
                    }
                }
                yield false;
            }
            default -> false;
        };
    }
}
