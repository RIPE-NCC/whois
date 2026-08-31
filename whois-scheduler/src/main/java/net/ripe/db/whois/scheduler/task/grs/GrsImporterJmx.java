package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.base.Strings;
import net.ripe.db.whois.common.jmx.JmxBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

@Component
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "GrsImport", description = "Whois GRS import")
public class GrsImporterJmx extends JmxBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrsImporterJmx.class);

    private final GrsImporter grsImporter;
    private final String grsDefaultSources;
    private final String grsPassphrase;

    @Autowired
    public GrsImporterJmx(
            final GrsImporter grsImporter,
            @Value("${grs.import.sources:}") final String grsDefaultSources,
            @Value("${grs.import.passphrase:}") final String validPassphrase) {
        super(LOGGER);
        this.grsImporter = grsImporter;
        this.grsDefaultSources = grsDefaultSources;
        this.grsPassphrase = validPassphrase;
    }

    @ManagedAttribute(description = "Comma separated list of default GRS sources")
    public String getGrsDefaultSources() {
        return grsDefaultSources;
    }

    @ManagedOperation(description = "Download new dumps and update GRS sources")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "sources", description = "Comma separated list of GRS sources to import (or 'all')"),
            @ManagedOperationParameter(name = "comment", description = "Optional comment for invoking the operation")
    })
    public String grsImport(final String sources, final String comment) {
        return invokeOperation("GRS import sources", comment, new Callable<String>() {
            @Override
            public String call() {
                grsImporter.grsImport("all".equals(sources) ? grsDefaultSources : sources, false);
                return "GRS import started";
            }
        });
    }

    @ManagedOperation(description = "Download new dumps and rebuild GRS sources")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "sources", description = "Comma separated list of GRS sources to import (or 'all')"),
            @ManagedOperationParameter(name = "passphrase", description = "The passphrase to prevent accidental invocation"),
            @ManagedOperationParameter(name = "comment", description = "Optional comment for invoking the operation")
    })
    public String grsRebuild(final String sources, final String passphrase, final String comment) {
        return invokeOperation("GRS rebuild sources", comment, new Callable<String>() {
            @Override
            public String call() {
                if (Strings.isNullOrEmpty(passphrase)) {
                    throw new IllegalStateException("Rebuild not allowed without a passphrase");
                }
                if (!passphrase.equals(grsPassphrase)) {
                    return """
                            Warning
                            Rebuild will delete all content in the specified
                            sources, when unsure use the grsImport() operation,
                            which will update the sources using diff.
                            When you are absolutely sure, the passphrase is specified in the properties
                            """;
                }

                grsImporter.grsImport("all".equals(sources) ? grsDefaultSources : sources, true);
                return "GRS rebuild started";
            }
        });
    }
}
