package net.ripe.db.whois.api.httpserver.dos;

import com.google.common.base.Joiner;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import org.eclipse.jetty.jmx.ObjectMBean;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlets.DoSFilter;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedOperation;
import org.eclipse.jetty.util.annotation.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractDoSFilterHolder extends FilterHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDoSFilterHolder.class);

    private static final Joiner COMMA_JOINER = Joiner.on(',');

    private final List<Ipv4Resource> ipv4whitelist = new CopyOnWriteArrayList<>();
    private final List<Ipv6Resource> ipv6whitelist = new CopyOnWriteArrayList<>();

    AbstractDoSFilterHolder(final boolean dosFilterEnabled, final String trustedIpRanges) {
        if (!dosFilterEnabled) {
            LOGGER.info("DoSFilter is *not* enabled");
        }

        final DoSFilter doSFilter = generateWhoisDoSFilter();

        createJmxBean(doSFilter);

        this.setFilter(doSFilter);
        this.setName(getFilerName());
        this.setInitParameter("enabled", String.valueOf(dosFilterEnabled));
        this.setInitParameter("delayMs", "-1"); // reject requests over threshold
        this.setInitParameter("remotePort", "false");
        this.setInitParameter("trackSessions", "false");
        this.setInitParameter("insertHeaders", "false");
        this.setInitParameter("ipWhitelist", trustedIpRanges);
    }


    protected abstract boolean isAllowedMethod(final HttpServletRequest request);

    protected abstract String getFilerName();

    private static void createJmxBean(DoSFilter doSFilter) {
        try {
            final ObjectName dosFilterMBeanName = ObjectName.getInstance("net.ripe.db.whois:name=DosFilter");
            if (!ManagementFactory.getPlatformMBeanServer().isRegistered(dosFilterMBeanName)) {
                ManagementFactory.getPlatformMBeanServer().registerMBean(new ObjectMBean(doSFilter), dosFilterMBeanName);
            }
        } catch (MalformedObjectNameException | NotCompliantMBeanException | InstanceAlreadyExistsException | MBeanRegistrationException e){
            LOGGER.error("Error creating DoSFilter bean", e);
            throw new IllegalStateException(e);
        }
    }

    protected DoSFilter generateWhoisDoSFilter(){
        return new DoSFilter(){
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
            @ManagedAttribute("list of IPs that will not be rate limited")
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
                    addWhitelistAddressWithoutLog(address);
                }
                logWhiteList();
            }

            /**
             * Clears the list of whitelisted IP addresses
             */
            @Override
            @ManagedOperation("clears the list of IP addresses that will not be rate limited")
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
            @ManagedOperation("adds an IP address that will not be rate limited")
            public boolean addWhitelistAddress(@Name("address") final String address) {
                logWhiteList();
                return addWhitelistAddressWithoutLog(address);
            }

            private boolean addWhitelistAddressWithoutLog(final String address) {
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
            @ManagedOperation("removes an IP address that will not be rate limited")
            public boolean removeWhitelistAddress(@Name("address") final String address) {
                logWhiteList();
                if (address.contains(".")) {
                    return ipv4whitelist.remove(Ipv4Resource.parse(address));
                } else {
                    return ipv6whitelist.remove(Ipv6Resource.parse(address));
                }
            }

            private void logWhiteList() {
                LOGGER.info("DoSFilter IP whitelist: {}", getWhitelist());
            }

            @Override
            public void doFilter(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws IOException, ServletException {
                if (isAllowedMethod(request)){
                    chain.doFilter(request, response);
                    return;
                }
                super.doFilter(request, response, chain);
            }
        };
    }
}
