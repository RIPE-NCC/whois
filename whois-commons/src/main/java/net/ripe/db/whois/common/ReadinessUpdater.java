package net.ripe.db.whois.common;

import net.ripe.db.whois.common.jmx.JmxBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

/**
 * in jmxterm, run with:
 *      bean net.ripe.db.whois:name=ReadinessUpdater
 *      run up - ready to receive traffic
 *      run down - not ready to receive traffic
 *
 */
@Component
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "ReadinessUpdater", description = "Loadbalancer switch")
public class ReadinessUpdater extends JmxBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReadinessUpdater.class);

    private boolean loadBalancerEnabled;

    public ReadinessUpdater() {
        super(LOGGER);
    }

    @ManagedOperation
    public void up(){
        this.loadBalancerEnabled = true;
        LOGGER.info("Marked service as ready to receive traffic");
    }

    @ManagedOperation
    public void down() {
        this.loadBalancerEnabled = false;
        LOGGER.info("Marked service as not ready to receive traffic");
    }

    public boolean isLoadBalancerEnabled() {
        return loadBalancerEnabled;
    }

}
