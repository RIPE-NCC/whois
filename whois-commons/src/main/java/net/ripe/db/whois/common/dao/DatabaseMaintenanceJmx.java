package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.dao.jdbc.IndexDao;
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
    private final IndexDao indexDao;

    @Autowired
    public DatabaseMaintenanceJmx(final RpslObjectUpdateDao updateDao, final IndexDao indexDao) {
        super(LOGGER);
        this.updateDao = updateDao;
        this.indexDao = indexDao;
    }

    @ManagedOperation(description = "Recovers a deleted object")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "objectId", description = "Id of the object to recover"),
            @ManagedOperationParameter(name = "comment", description = "Optional comment for invoking the operation")
    })
    public String undeleteObject(final int objectId, final String comment) {
        return invokeOperation("Undelete Object", comment, new Callable<String>() {
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

    @ManagedOperation(description = "Rebuild all indexes based on objects in last")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "comment", description = "Optional comment for invoking the operation")
    })
    public void rebuildIndexes(final String comment) {
        backgroundOperation("Rebuild indexes", comment, new Callable<Void>() {
            @Override
            public Void call() {
                indexDao.rebuild();
                return null;
            }
        });
    }

    @ManagedOperation(description = "Rebuild indexes for specified object")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "objectId", description = "Id of the object to rebuild"),
            @ManagedOperationParameter(name = "comment", description = "Optional comment for invoking the operation")
    })
    public String rebuildIndexesForObject(final int objectId, final String comment) {
        return invokeOperation("Rebuild indexes for object: " + objectId, comment, new Callable<String>() {
            @Override
            public String call() throws Exception {
                indexDao.rebuildForObject(objectId);
                return "Rebuilt indexes for object: " + objectId;
            }
        });
    }

    @ManagedOperation(description = "Pause rebuild indexes")
    public void pause() {
        invokeOperation("Pause rebuild indexes", "", new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                indexDao.pause();
                return null;
            }
        });
    }

    @ManagedOperation(description = "Resume rebuild indexes")
    public void resume() {
        invokeOperation("Resume rebuild indexes", "", new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                indexDao.resume();
                return null;
            }
        });
    }
}
