package net.ripe.db.whois.api.fulltextsearch;

import net.ripe.db.whois.common.jmx.JmxBase;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.getLogger;

@Component
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "ElasticSearchRebuildIndex", description = "Rebuild full text search indexes")
public class ElasticSearchRebuildIndexJmx extends JmxBase {

    private static final Logger LOGGER = getLogger(ElasticSearchRebuildIndexJmx.class);

    private final ElasticFullTextRebuild elasticFullTextRebuild;

    @Autowired
    public ElasticSearchRebuildIndexJmx(final ElasticFullTextRebuild elasticFullTextRebuild) {
        super(LOGGER);
        this.elasticFullTextRebuild = elasticFullTextRebuild;
    }

    @ManagedOperation(description = "Rebuild full text search indexes")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "comment", description = "Comment for invoking the operation"),
    })
    public String runRebuildIndexes(final String comment) {
        return invokeOperation("rebuild ES indexes", comment, () -> {
            try {
                elasticFullTextRebuild.run();
            } catch (Exception ex) {
                LOGGER.error("Error while rebuilding ES indexes {}", ex.getMessage());
                return "unable to rebuild ES indexes, check whois logs for further info ";
            }

            return "Successfully rebuild indexes";
        });
    }
}
