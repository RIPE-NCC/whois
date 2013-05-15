package net.ripe.db.whois.common.dao;

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
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "DatabaseMaintenance", description = "Whois database maintenance operations")
public class DatabaseMaintenanceJmx extends JmxBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseMaintenanceJmx.class);

    private final RpslObjectUpdateDao updateDao;

    @Autowired
    public DatabaseMaintenanceJmx(final RpslObjectUpdateDao updateDao) {
        super(LOGGER);
        this.updateDao = updateDao;
    }

    @ManagedOperation(description = "Recovers a deleted object")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "objectId", description = "Id of the object to recover"),
            @ManagedOperationParameter(name = "comment", description = "Optional comment for invoking the operation")
    })
    public String undeleteObject(final int objectId, final String comment) {
        return invokeOperation("Unref cleanup", comment, new Callable<String>() {
            @Override
            public String call() {
                try {
                    final RpslObjectUpdateInfo updateInfo = updateDao.undeleteObject(objectId);
                    return String.format("Recovered object: %s", updateInfo);
                } catch (RuntimeException e) {
                    LOGGER.error("Unable to recover object with id: {}", objectId, e);
                    return String.format("Unable to recover: %s", e.getMessage());
                }
            }
        });
    }
}
