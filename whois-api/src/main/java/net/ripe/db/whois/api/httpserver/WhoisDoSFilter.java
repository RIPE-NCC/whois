package net.ripe.db.whois.api.httpserver;

import com.google.common.base.Joiner;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import org.eclipse.jetty.servlets.DoSFilter;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.annotation.Name;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Extends the {@link DoSFilter} from Jetty for support of IP ranges and better support for CIDR ranges using our
 * own {@link net.ripe.db.whois.common.ip.IpInterval} classes. (
 */
public class WhoisDoSFilter extends DoSFilter {

    private static final Logger LOGGER = getLogger(WhoisDoSFilter.class);

    private static final Joiner COMMA_JOINER = Joiner.on(',');

    private final List<Ipv4Resource> ipv4whitelist = new CopyOnWriteArrayList<>();
    private final List<Ipv6Resource> ipv6whitelist = new CopyOnWriteArrayList<>();

    private final String filterName;

    public WhoisDoSFilter(final String filterName){
        this.filterName = filterName;
    }

    @Override
    public void doFilter(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if (canProceed(request)){
            super.doFilter(request, response, chain);
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    protected boolean checkWhitelist(final String candidate) {
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
    @Override
    public String getWhitelist() {
        StringBuilder result = new StringBuilder();

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
        for (String address : StringUtil.csvSplit(commaSeparatedList)) {
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
        LOGGER.info("DoSFilter IP whitelist cleared");
    }

    /**
     * Adds the given IP address, either in the form of a dotted decimal notation A.B.C.D
     * or in the CIDR notation A.B.C.D/M, to the list of whitelisted IP addresses.
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
        LOGGER.info("DoSFilter IP whitelist: {}", getWhitelist());
    }

    private boolean canProceed(final HttpServletRequest request) {
        if (request == null) {
            return  false;
        }

        if (filterName.equalsIgnoreCase("lookupFilter")) {
            return request.getMethod().equalsIgnoreCase("GET");
        }

        if (filterName.equalsIgnoreCase("updateFilter")) {
            return !request.getMethod().equalsIgnoreCase("GET");
        }

        return false;
    }
}
