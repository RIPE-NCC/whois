package net.ripe.db.whois.api.httpserver.hazelcast;

import net.ripe.db.whois.api.httpserver.JettyBootstrap;
import org.eclipse.jetty.util.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource(objectName = JettyBootstrap.BLOCK_LIST_JMX_NAME)
public interface WhoisHazelcastManagerFilterMBean {

    @ManagedOperation("adds an IP address to blocked list")
    String addBlockedListAddress(final String address);

    @ManagedOperation("Remove an IP address to blocked list")
    String removeBlockedListAddress(final String address);

    @ManagedOperation("Retrieve blocked list")
    String getBlockedList();
}
