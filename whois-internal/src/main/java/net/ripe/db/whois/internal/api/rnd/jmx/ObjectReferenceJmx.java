package net.ripe.db.whois.internal.api.rnd.jmx;

import net.ripe.db.whois.common.jmx.JmxBase;
import net.ripe.db.whois.internal.api.rnd.UpdateObjectVersions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

@Component
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "ObjectReference", description = "Object Reference operations")
public class ObjectReferenceJmx extends JmxBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectReferenceJmx.class);

    private final UpdateObjectVersions updateObjectVersions;

    @Autowired
    public ObjectReferenceJmx(final UpdateObjectVersions updateObjectVersions) {
        super(LOGGER);
        this.updateObjectVersions = updateObjectVersions;
    }

    @ManagedOperation(description = "incremental update of object references")
    public String update() {
        invokeOperation("incremental update of object references", "", new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                updateObjectVersions.run();
                return null;
            }
        });
        return "Updated";
    }
}
