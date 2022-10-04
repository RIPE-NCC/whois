package net.ripe.db.whois.api.healthcheck;

import net.ripe.db.whois.common.HealthCheck;
import net.ripe.db.whois.common.jmx.JmxBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

@Component
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "HealthCheck", description = "JMX health check operations")
public class HealthCheckJmx extends JmxBase implements HealthCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckJmx.class);

    private boolean enabled = true;

    @Autowired
    public HealthCheckJmx() {
        super(LOGGER);
    }

    @Override
    @ManagedOperation(description = "Get JMX healthcheck status")
    public boolean check() {
        return enabled;
    }

    @ManagedOperation(description = "Enable JMX healthcheck")
    public void enable() {
        this.enabled = true;
    }

    @ManagedOperation(description = "Disable JMX healthcheck")
    public void disable() {
        this.enabled = false;
    }
}
