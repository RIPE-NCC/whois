package net.ripe.db.nrtm4;

import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.loadScripts;


@Tag("IntegrationTest")
@ContextConfiguration(locations = {"classpath:applicationContext-nrtm4-test.xml"})
public class NrtmFileProcessorIntegrationTest extends AbstractDatabaseHelperIntegrationTest {

    @Autowired
    private NrtmFileProcessor nrtmFileProcessor;

    @Test
    void run_nrtm_write_job() {
        loadScripts(whoisTemplate, "nrtm_sample_sm.sql");
        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();
    }

}
