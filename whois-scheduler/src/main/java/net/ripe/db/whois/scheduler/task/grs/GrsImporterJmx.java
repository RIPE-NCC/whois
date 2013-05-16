package net.ripe.db.whois.scheduler.task.grs;

import net.ripe.db.whois.common.jmx.JmxBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

@Component
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "GrsImport", description = "Whois GRS import")
public class GrsImporterJmx extends JmxBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrsImporterJmx.class);

    private final GrsImporter grsImporter;

    private String grsDefaultSources;

    @Value("${grs.import.sources}")
    void setGrsDefaultSources(final String grsDefaultSources) {
        this.grsDefaultSources = grsDefaultSources;
    }

    @Autowired
    public GrsImporterJmx(final GrsImporter grsImporter) {
        super(LOGGER);
        this.grsImporter = grsImporter;
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
                final String validPassphrase = "grsrebuildnow";
                if (!passphrase.equals(validPassphrase)) {
                    return String.format("" +
                            "Warning:\n\n" +
                            "Rebuild will delete all content in the specified\n" +
                            "sources, when unsure use the grsImport() operation,\n" +
                            "which will update the sources using diff.\n\n" +
                            "When you are absolutely sure, specify the\n" +
                            "passphrase: %s", validPassphrase);
                }

                grsImporter.grsImport("all".equals(sources) ? grsDefaultSources : sources, true);
                return "GRS rebuild started";
            }
        });
    }
}
