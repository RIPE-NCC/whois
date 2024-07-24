package net.ripe.db.whois.api.httpserver;

import com.google.common.base.Joiner;
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
import org.eclipse.jetty.util.annotation.Name;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.slf4j.LoggerFactory.getLogger;

public class WhoisBlockedListFilter implements Filter {

    private static final Logger LOGGER = getLogger(WhoisDoSFilter.class);

    private static final Joiner COMMA_JOINER = Joiner.on(',');

    private final List<Ipv4Resource> ipv4blockedlist = new CopyOnWriteArrayList<>();

    private final List<Ipv6Resource> ipv6blockedlist = new CopyOnWriteArrayList<>();

    public WhoisBlockedListFilter(final String commaSeparatedList){
        for (final String address : StringUtil.csvSplit(commaSeparatedList)) {
            addBlockedListAddress(address);
        }
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
        ipv4blockedlist.clear();
        ipv6blockedlist.clear();
        LOGGER.info("Blocked listed IPs have been removed");
    }

    @ManagedOperation("adds an IP address to blocked list")
    public void addBlockedListAddress(@Name("address") final String address) {
        if (address.contains(".")) {
            ipv4blockedlist.add(Ipv4Resource.parse(address));
        } else {
            ipv6blockedlist.add(Ipv6Resource.parse(address));
        }
        LOGGER.info("Ipaddress {} added to blocked list", address);
    }

    @ManagedOperation("Remove an IP address to blocked list")
    public void removeBlockedListAddress(@Name("address") final String address) {
        if (address.contains(".")){
            ipv4blockedlist.remove(Ipv4Resource.parse(address));
        } else {
            ipv6blockedlist.remove(Ipv6Resource.parse(address));
        }
        LOGGER.info("Ipaddress {} removed from blocked list", address);
    }

    @ManagedOperation("Retrieve blocked list")
    public void getWhitelist() {
        StringBuilder result = new StringBuilder();

        COMMA_JOINER.appendTo(result, ipv4blockedlist);
        result.append(',');
        COMMA_JOINER.appendTo(result, ipv6blockedlist);

        LOGGER.info("The blocked list contains next IPs {}", result);
    }

    private boolean isBlockedIp(final String candidate) {
        // TODO: Duplicated in WhoisDoSFilter, maybe put this into a utils or into a interface with a default method
        if (candidate.contains(".")) {
            final Ipv4Resource address = Ipv4Resource.parse(candidate);
            for (Ipv4Resource entry : ipv4blockedlist) {
                if (entry.contains(address)) {
                    return true;
                }
            }
        } else {
            final Ipv6Resource address = Ipv6Resource.parse(candidate);
            for (Ipv6Resource entry : ipv6blockedlist) {
                if (entry.contains(address)) {
                    return true;
                }
            }
        }

        return false;
    }
}
