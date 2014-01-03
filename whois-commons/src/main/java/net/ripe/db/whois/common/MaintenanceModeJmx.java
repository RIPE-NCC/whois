package net.ripe.db.whois.common;

import net.ripe.db.whois.common.dao.DatabaseMaintenanceJmx;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.jmx.JmxBase;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

@Component
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "MaintenanceMode", description = "Enable/disable accepting new requests for all whois services")
public class MaintenanceModeJmx extends JmxBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(MaintenanceModeJmx.class);

    private final MaintenanceMode maintenanceMode;
    private final IpRanges ipRanges;

    @Autowired
    public MaintenanceModeJmx(final MaintenanceMode maintenanceMode, IpRanges ipRanges) {
        super(LOGGER);
        this.maintenanceMode = maintenanceMode;
        this.ipRanges = ipRanges;
    }

    @ManagedOperation(description = "Sets maintenance mode")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "mode", description = "Access rights for 'world,trusted' (values: FULL/READONLY/NONE) (e.g. 'none,full')"),
    })
    public void setMaintenanceMode(final String mode) {
        invokeOperation("Set maintenance mode", mode, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                maintenanceMode.set(mode);
                return null;
            }
        });
    }

    @ManagedOperation(description = "Sets trusted range (ipranges.trusted)")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "ranges", description = "Comma-separated list of IP prefixes/ranges (e.g. '10/8,::1/64)"),
    })
    public void setTrustedIpRanges(final String ranges) {
        invokeOperation("Set trusted ranges", ranges, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ipRanges.setTrusted(StringUtils.split(ranges, ','));
                return null;
            }
        });
    }
}
