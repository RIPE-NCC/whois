package net.ripe.db.nrtm4.jmx;

import org.springframework.jmx.export.annotation.ManagedOperation;


public interface NrtmProcessControl {

    @ManagedOperation(description = "Get JMX initial snapshot generation status")
    boolean isInitialSnapshotGenerationEnabled();

    @ManagedOperation(description = "Enable JMX initial snapshot generation")
    void enableInitialSnapshotGeneration();

    @ManagedOperation(description = "Disable JMX initial snapshot generation")
    void disableInitialSnapshotGeneration();

}
