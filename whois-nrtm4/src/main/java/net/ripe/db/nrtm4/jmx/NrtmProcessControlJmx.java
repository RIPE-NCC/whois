package net.ripe.db.nrtm4.jmx;

import net.ripe.db.whois.common.jmx.JmxBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;


@Component
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "SnapshotInitializer", description = "Trigger creation of initial snapshot")
public class NrtmProcessControlJmx extends JmxBase implements NrtmProcessControl {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmProcessControlJmx.class);
    private boolean initialSnapshotGenerationIsEnabled = false;

    public NrtmProcessControlJmx() {
        super(LOGGER);
    }

    @Override
    @ManagedOperation(description = "Get JMX initial snapshot generation status")
    public boolean isInitialSnapshotGenerationEnabled() {
        return initialSnapshotGenerationIsEnabled;
    }

    @Override
    @ManagedOperation(description = "Enable JMX initial snapshot generation")
    public void enableInitialSnapshotGeneration() {
        this.initialSnapshotGenerationIsEnabled = true;
    }

    @Override
    @ManagedOperation(description = "Disable JMX initial snapshot generation")
    public void disableInitialSnapshotGeneration() {
        this.initialSnapshotGenerationIsEnabled = false;
    }

}
