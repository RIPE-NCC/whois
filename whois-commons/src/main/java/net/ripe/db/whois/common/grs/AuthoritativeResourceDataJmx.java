package net.ripe.db.whois.common.grs;

import net.ripe.db.whois.common.jmx.JmxBase;
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
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "AuthoritativeResources", description = "Whois authoritative resource data")
public class AuthoritativeResourceDataJmx extends JmxBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthoritativeResourceDataJmx.class);

    private final AuthoritativeResourceData authoritativeResourceData;

    @Autowired
    public AuthoritativeResourceDataJmx(final AuthoritativeResourceData authoritativeResourceData) {
        super(LOGGER);
        this.authoritativeResourceData = authoritativeResourceData;
    }

    @ManagedOperation(description = "Refresh authoritative resource cache")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "comment", description = "Optional comment for invoking the operation")
    })
    public String refreshCache(final String comment) {
        return invokeOperation("Refresh authoritative resource cache", comment, new Callable<String>() {
            @Override
            public String call() {
                authoritativeResourceData.refreshAuthoritativeResourceCache();
                return "Refreshed caches";
            }
        });
    }
}
