package net.ripe.db.whois.scheduler.task.loader;

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
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "Bootstrap", description = "Whois database bootstrap")
public class BootstrapJmx extends JmxBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(BootstrapJmx.class);

    private final Bootstrap bootstrap;

    @Autowired
    public BootstrapJmx(final Bootstrap bootstrap) {
        super(LOGGER);
        this.bootstrap = bootstrap;
    }

    @ManagedOperation(description = "Load text dump into main database (only adds new objects), \n" +
            "slow but safe, transactional, it DOES NOT use global update lock!)")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "comment", description = "Optional comment for invoking the operation"),
            @ManagedOperationParameter(name = "filenames", description = "Comma separated list of paths to the dump files")
    })
    public String loadDump(final String comment, final String filenames) {
        return invokeOperation("Load dump", comment, new Callable<String>() {
            @Override
            public String call() {
                return bootstrap.loadTextDumpSafe(filenames.split(","));
            }
        });
    }

    @ManagedOperation(description = "Load text dump into main database (only adds new objects), \n" +
            "fast but risky, non transactional (support for big dumps), it DOES NOT use global update lock!)")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "comment", description = "Optional comment for invoking the operation"),
            @ManagedOperationParameter(name = "filenames", description = "Comma separated list of paths to the dump files")
    })
    public String loadDumpRisky(final String comment, final String filenames) {
        return invokeOperation("Load dump", comment, new Callable<String>() {
            @Override
            public String call() {
                return bootstrap.loadTextDumpRisky(filenames.split(","));
            }
        });
    }

    @ManagedOperation(description = "Run nightly bootstrap (destructive, deletes database before load)  (DOES NOT use global update lock!)")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "comment", description = "Optional comment for invoking the operation")
    })
    public String resetTestDatabase(final String comment) {
        return invokeOperation("Reset test database", comment, new Callable<String>() {
            @Override
            public String call() {
                return bootstrap.bootstrap();
            }
        });
    }

}
