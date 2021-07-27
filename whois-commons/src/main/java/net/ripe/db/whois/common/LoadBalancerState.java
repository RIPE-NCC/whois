package net.ripe.db.whois.common;

import net.ripe.db.whois.common.jmx.JmxBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

/**
 * in jmxterm, run with:
 *      bean net.ripe.db.whois:name=LoadBalancer
 *      run setLoadBalancerEnabled true|false
 *
 */
@Component
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "LoadBalancer", description = "Loadbalancer switch")
public class LoadBalancerState extends JmxBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadBalancerState.class);

    private boolean loadBalancerEnabled;

    public LoadBalancerState() {
        super(LOGGER);
    }

    @ManagedAttribute(description = "true to enable on load balancer. False otherwise")
    public void setLoadBalancerEnabled(boolean loadBalancerEnabled) {
        this.loadBalancerEnabled = loadBalancerEnabled;
    }

    public boolean isLoadBalancerEnabled() {
        return loadBalancerEnabled;
    }

}
