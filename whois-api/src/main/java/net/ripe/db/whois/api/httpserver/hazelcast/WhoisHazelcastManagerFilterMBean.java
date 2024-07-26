package net.ripe.db.whois.api.httpserver.hazelcast;

import org.eclipse.jetty.util.annotation.ManagedOperation;

public interface WhoisHazelcastManagerFilterMBean {

    @ManagedOperation("adds an IP address to blocked list")
    String addBlockedListAddress(final String address);

    @ManagedOperation("Remove an IP address to blocked list")
    String removeBlockedListAddress(final String address);

    @ManagedOperation("Retrieve blocked list")
    String getBlockedList();
}
