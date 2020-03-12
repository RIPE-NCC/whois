package net.ripe.db.whois.api.httpserver;

import com.google.common.base.Joiner;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import org.eclipse.jetty.servlets.DoSFilter;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedOperation;
import org.eclipse.jetty.util.annotation.Name;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.slf4j.LoggerFactory.getLogger;

public class WhoisDoSFilter extends DoSFilter {

    private static final Logger LOGGER = getLogger(WhoisDoSFilter.class);

    private final List<Ipv4Resource> ipv4whitelist = new CopyOnWriteArrayList<>();
    private final List<Ipv6Resource> ipv6whitelist = new CopyOnWriteArrayList<>();

    protected boolean checkWhitelist(String candidate) {
        if (candidate.contains(".")) {
            final Ipv4Resource address = Ipv4Resource.parse(candidate);
            for (Ipv4Resource entry : ipv4whitelist) {
                if (entry.contains(address)) {
                    return true;
                }
            }
        } else {
            final Ipv6Resource address = Ipv6Resource.parse(candidate);
            for (Ipv6Resource entry : ipv6whitelist) {
                if (entry.contains(address)) {
                    return true;
                }
            }
        }

        return false;
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
    @ManagedAttribute("list of IPs that will not be rate limited")
    public String getWhitelist() {
        StringBuilder result = new StringBuilder();

        Joiner.on(',').appendTo(result, ipv4whitelist);
        Joiner.on(',').appendTo(result, ipv6whitelist);

        return result.toString();
    }

    /**
     * Set a list of IP addresses that will not be rate limited.
     *
     * @param commaSeparatedList comma-separated whitelist
     */
    public void setWhitelist(String commaSeparatedList) {
        clearWhitelist();
        List<String> result = new ArrayList<>();
        for (String address : StringUtil.csvSplit(commaSeparatedList)) {
            addWhitelistAddress(address);
        }
        LOGGER.debug("Whitelisted IP addresses: {}", result);
    }

    /**
     * Clears the list of whitelisted IP addresses
     */
    @ManagedOperation("clears the list of IP addresses that will not be rate limited")
    public void clearWhitelist() {
        ipv4whitelist.clear();
        ipv6whitelist.clear();
    }

    /**
     * Adds the given IP address, either in the form of a dotted decimal notation A.B.C.D
     * or in the CIDR notation A.B.C.D/M, to the list of whitelisted IP addresses.
     *
     * @param address the address to add
     * @return whether the address was added to the list
     * @see #removeWhitelistAddress(String)
     */
    @ManagedOperation("adds an IP address that will not be rate limited")
    public boolean addWhitelistAddress(@Name("address") String address) {
        if (address.contains(".")) {
            return ipv4whitelist.add(Ipv4Resource.parse(address));
        } else {
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
    @ManagedOperation("removes an IP address that will not be rate limited")
    public boolean removeWhitelistAddress(@Name("address") String address){
        if (address.contains(".")) {
            return ipv4whitelist.remove(Ipv4Resource.parse(address));
        } else {
            return ipv6whitelist.remove(Ipv6Resource.parse(address));
        }
    }

}
