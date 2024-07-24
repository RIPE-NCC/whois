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

public class WhoisBlackListFilter implements Filter {

    private static final Logger LOGGER = getLogger(WhoisDoSFilter.class);

    private static final Joiner COMMA_JOINER = Joiner.on(',');

    private final List<Ipv4Resource> ipv4blacklist = new CopyOnWriteArrayList<>();

    private final List<Ipv6Resource> ipv6blacklist = new CopyOnWriteArrayList<>();

    public WhoisBlackListFilter(final String commaSeparatedList){
        for (final String address : StringUtil.csvSplit(commaSeparatedList)) {
            addBlackListAddress(address);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        final HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        if (isBlackIp(httpRequest.getRemoteAddr())){
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS_429);
            httpResponse.getWriter().write("You have been permanently blocked. Please contact support");
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        ipv4blacklist.clear();
        ipv6blacklist.clear();
        LOGGER.info("Blacklisted IPs have been removed");
    }

    @ManagedOperation("adds an IP address to black list")
    public void addBlackListAddress(@Name("address") final String address) {
        if (address.contains(".")) {
            ipv4blacklist.add(Ipv4Resource.parse(address));
        } else {
            ipv6blacklist.add(Ipv6Resource.parse(address));
        }
        LOGGER.info("Ipaddress {} added to black list", address);
    }

    @ManagedOperation("Remove an IP address to black list")
    public void removeBlacklistAddress(@Name("address") final String address) {
        if (address.contains(".")){
            ipv4blacklist.remove(Ipv4Resource.parse(address));
        } else {
            ipv6blacklist.remove(Ipv6Resource.parse(address));
        }
        LOGGER.info("Ipaddress {} removed from black list", address);
    }

    @ManagedOperation("Retrieve black list")
    public void getWhitelist() {
        StringBuilder result = new StringBuilder();

        COMMA_JOINER.appendTo(result, ipv4blacklist);
        result.append(',');
        COMMA_JOINER.appendTo(result, ipv6blacklist);

        LOGGER.info("The blacklist contains next IPs {}", result);
    }

    private IpInterval<?> getIntervalOfIpAddress(final String address){
        if (address.contains(".")){
            return Ipv4Resource.parse(address);
        } else {
            return Ipv6Resource.parse(address);
        }
    }

    private boolean isBlackIp(final String remoteIp) {
        final IpInterval<?> addressInterval = getIntervalOfIpAddress(remoteIp);
        return addressInterval instanceof Ipv4Resource && ipv4blacklist.contains(addressInterval) ||
                addressInterval instanceof Ipv6Resource && ipv6blacklist.contains(addressInterval);
    }
}
