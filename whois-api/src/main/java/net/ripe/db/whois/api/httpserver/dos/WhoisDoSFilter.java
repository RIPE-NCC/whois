package net.ripe.db.whois.api.httpserver.dos;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.net.InetAddresses;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import org.eclipse.jetty.ee11.servlets.DoSFilter;
import org.eclipse.jetty.util.annotation.Name;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Extends the {@link DoSFilter} from Jetty for support of IP ranges and better support for CIDR ranges using our
 * own {@link net.ripe.db.whois.common.ip.IpInterval} classes. (
 */
public abstract class WhoisDoSFilter extends DoSFilter {

    private static final Joiner COMMA_JOINER = Joiner.on(',');
    private static final Splitter COMMA_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();

    private final Set<Ipv4Resource> ipv4whitelist = new CopyOnWriteArraySet<>();
    private final Set<Ipv6Resource> ipv6whitelist = new CopyOnWriteArraySet<>();
    private final Logger logger;
    private final String limit;

    public WhoisDoSFilter(final Logger logger, final String limit) {
        this.logger = logger;
        this.limit = limit;
    }

    @Override
    public void doFilter(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if (!canProceed(request)) {
            chain.doFilter(request, response);
            return;
        }

        final InetAddress inetAddress = AccessControlListManager.mask(InetAddresses.forString(request.getRemoteAddr()), AccessControlListManager.IPV6_NETMASK);

        if (inetAddress instanceof Inet4Address) {
            // Still using DoSFilter for whiteListed because MAX_REQUEST_MS_INIT_PARAM timeouts logic.
            super.doFilter(request, response, chain);
            return;
        }

        final Ipv6Resource interval = Ipv6Resource.parse(inetAddress.getHostAddress() + "/" + AccessControlListManager.IPV6_NETMASK);
        doFilterGroupingByMaskedAddress(request, response, chain, interval.toString());
    }

    private void doFilterGroupingByMaskedAddress(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain, final String maskedAddress) throws IOException,
            ServletException {
        if (maskedAddress == null) {
            super.doFilter(request, response, chain);
            return;
        }

        final HttpServletRequest wrapped = new HttpServletRequestWrapper(request) {
            @Override
            public String getRemoteAddr() {
                return maskedAddress;
            }
        };

        super.doFilter(wrapped, response, (req, res) -> chain.doFilter(request, res));
    }

    /**
     * Check a list of whitelisted IP addresses.
     * Ipv6 whitelist is containing /64 or less specific, so it is consistent with ACL and DoSFilter
     *
     * @return true if the IP address is whitelisted, false otherwise
     */
    @Override
    protected boolean checkWhitelist(final String candidate) {
        return switch (IpInterval.parse(candidate)) {
            case Ipv4Resource ipv4Resource -> ipv4whitelist.stream().anyMatch(entry -> entry.contains(ipv4Resource));
            case Ipv6Resource ipv6Resource -> ipv6whitelist.stream().anyMatch(entry -> entry.contains(ipv6Resource));
        };
    }

    @Override
    public void destroy() {
        clearWhitelist();
        super.destroy();
    }

    /**
     * Get a list of IP addresses that will not be rate limited.
     *
     * @return comma-separated whitelist
     */
    @Override
    public String getWhitelist() {
        final StringBuilder result = new StringBuilder();
        COMMA_JOINER.appendTo(result, ipv4whitelist);
        result.append(',');
        COMMA_JOINER.appendTo(result, ipv6whitelist);
        return result.toString();
    }

    /**
     * Set a list of IP addresses that will not be rate limited.
     *
     * @param commaSeparatedList comma-separated whitelist
     */
    @Override
    public void setWhitelist(final String commaSeparatedList) {
        clearWhitelist();
        for (String address : COMMA_SPLITTER.split(commaSeparatedList)) {
            addWhitelistAddress(address);
        }
        logWhiteList();
    }

    /**
     * Clears the list of whitelisted IP addresses
     */
    @Override
    public void clearWhitelist() {
        ipv4whitelist.clear();
        ipv6whitelist.clear();
        logger.info("DoSFilter IP whitelist cleared");
    }

    /**
     * Adds the given IP address, either in the form of a dotted decimal notation A.B.C.D
     * or in the CIDR notation A.B.C.D/M, to the list of whitelisted IP addresses.
     * IPv6 must be /64 or less specific.
     *
     * @param address the address to add
     * @return whether the address was added to the list
     * @see #removeWhitelistAddress(String)
     */
    @Override
    public boolean addWhitelistAddress(@Name("address") final String address) {
        if (address.contains(".")) {
            return ipv4whitelist.add(Ipv4Resource.parse(address));
        } else {
            final Ipv6Resource resource = Ipv6Resource.parse(address);
            if (resource.getPrefixLength() > AccessControlListManager.IPV6_NETMASK) { //If more specific than /64, we need to mask it
                final InetAddress inetAddress = AccessControlListManager.mask(InetAddresses.forString(address), AccessControlListManager.IPV6_NETMASK);
                return ipv6whitelist.add(Ipv6Resource.parse(inetAddress.getHostAddress() + "/" + AccessControlListManager.IPV6_NETMASK));
            }
            return ipv6whitelist.add(Ipv6Resource.parse(address));
        }
    }

    /**
     * Removes the given address from the list of whitelisted IP addresses.
     *
     * @param address the address to remove
     * @return whether the address was removed from the list
     * @see #addWhitelistAddress(String)
     */
    @Override
    public boolean removeWhitelistAddress(@Name("address") final String address) {
        final boolean isRemoved = address.contains(".") ? ipv4whitelist.remove(Ipv4Resource.parse(address)) :
                ipv6whitelist.remove(Ipv6Resource.parse(address));
        logWhiteList();
        return isRemoved;
    }


    private void logWhiteList() {
        logger.info("DoSFilter IP whitelist: {}", getWhitelist());
    }

    protected abstract boolean canProceed(final HttpServletRequest request);

    public String getLimit(){
        return limit;
    };
}
