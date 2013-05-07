package net.ripe.db.whois.scheduler.task.unref;

import net.ripe.db.whois.common.jmx.JmxBase;
import net.ripe.db.whois.scheduler.task.grs.GrsImporterJmx;
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
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "UnrefCleanup", description = "Whois unreferenced object cleanup")
public class UnrefCleanupJmx extends JmxBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrsImporterJmx.class);

    private final UnrefCleanup unrefCleanup;

    @Autowired
    public UnrefCleanupJmx(final UnrefCleanup unrefCleanup) {
        super(LOGGER);
        this.unrefCleanup = unrefCleanup;
    }

    @ManagedOperation(description = "Unreferenced object cleanup")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "comment", description = "Optional comment for invoking the operation")
    })
    public String unrefCleanup(final String comment) {
        return invokeOperation("Unref cleanup", comment, new Callable<String>() {
            @Override
            public String call() {
                unrefCleanup.run();
                return "Unref cleanup started";
            }
        });
    }
}
