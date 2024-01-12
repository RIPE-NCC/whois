package net.ripe.db.whois.common.grs;

import net.ripe.db.whois.common.jmx.JmxBase;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Component
@Profile({WhoisProfile.DEPLOYED})
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "AuthoritativeResources", description = "Whois authoritative resource data")
public class AuthoritativeResourceDataJmx extends JmxBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthoritativeResourceDataJmx.class);

    private final AuthoritativeResourceRefreshTask authoritativeResourceRefreshTask;
    private final AuthoritativeResourceDataValidator authoritativeResourceDataValidator;

    @Autowired
    public AuthoritativeResourceDataJmx(final AuthoritativeResourceRefreshTask authoritativeResourceRefreshTask, final AuthoritativeResourceDataValidator authoritativeResourceDataValidator) {
        super(LOGGER);
        this.authoritativeResourceRefreshTask = authoritativeResourceRefreshTask;
        this.authoritativeResourceDataValidator = authoritativeResourceDataValidator;
    }

    @ManagedOperation(description = "Refresh authoritative resource cache")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "comment", description = "Optional comment for invoking the operation")
    })
    public String refreshCache(final String comment) {
        return invokeOperation("Refresh authoritative resource cache", comment, () -> {
            authoritativeResourceRefreshTask.refreshGrsAuthoritativeResourceCaches();
            return "Refreshed caches";
        });
    }

    @ManagedOperation(description = "Check overlaps in authoritative resource definitions and output to file")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "outputFile", description = "The file to write overlaps to"),
            @ManagedOperationParameter(name = "comment", description = "Optional comment for invoking the operation")
    })
    public String checkOverlaps(final String outputFile, final String comment) {
        return invokeOperation(String.format("Writing overlaps to %s", outputFile), comment, () -> {
            final File output = new File(outputFile);
            final String absolutePath = output.getAbsolutePath();


            try {
                if (!output.createNewFile()) {
                    return String.format("Abort, file already exists: %s", absolutePath);
                }

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
                    authoritativeResourceDataValidator.checkOverlaps(writer);
                }
            } catch (IOException e) {
                LOGGER.error("Checking overlaps", e);
                return String.format("Failed writing to: %s, %s", absolutePath, e.getMessage());
            }

            return String.format("Overlaps written to: %s", absolutePath);
        });
    }
}
