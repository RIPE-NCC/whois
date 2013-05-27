package net.ripe.db.whois.common.grs;

import net.ripe.db.whois.common.jmx.JmxBase;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Callable;

@Component
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "AuthoritativeResources", description = "Whois authoritative resource data")
public class AuthoritativeResourceDataJmx extends JmxBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthoritativeResourceDataJmx.class);

    private final AuthoritativeResourceData authoritativeResourceData;
    private final AuthoritativeResourceDataValidator authoritativeResourceDataValidator;

    @Autowired
    public AuthoritativeResourceDataJmx(final AuthoritativeResourceData authoritativeResourceData, final AuthoritativeResourceDataValidator authoritativeResourceDataValidator) {
        super(LOGGER);
        this.authoritativeResourceData = authoritativeResourceData;
        this.authoritativeResourceDataValidator = authoritativeResourceDataValidator;
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

    @ManagedOperation(description = "Check overlaps in authoritative resource definitions and output to file")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "outputFile", description = "The file to write overlaps to"),
            @ManagedOperationParameter(name = "comment", description = "Optional comment for invoking the operation")
    })
    public String checkOverlaps(final String outputFile, final String comment) {
        return invokeOperation(String.format("Writing overlaps to %s", outputFile), comment, new Callable<String>() {
            @Override
            public String call() {
                final File output = new File(outputFile);
                final String absolutePath = output.getAbsolutePath();


                BufferedWriter writer = null;
                try {
                    if (!output.createNewFile()) {
                        return String.format("Abort, file already exists: %s", absolutePath);
                    }

                    writer = new BufferedWriter(new FileWriter(output));
                    authoritativeResourceDataValidator.checkOverlaps(writer);
                } catch (IOException e) {
                    LOGGER.error("Checking overlaps", e);
                    return String.format("Failed writing to: %s, %s", absolutePath, e.getMessage());
                } finally {
                    IOUtils.closeQuietly(writer);
                }

                return String.format("Overlaps written to: %s", absolutePath);
            }
        });
    }
}
