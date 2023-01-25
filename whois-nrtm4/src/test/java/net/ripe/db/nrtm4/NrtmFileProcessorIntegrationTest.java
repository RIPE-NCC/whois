package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.dao.NrtmSourceHolder;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.jmx.NrtmProcessControlJmx;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.loadScripts;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


@Tag("IntegrationTest")
public class NrtmFileProcessorIntegrationTest extends AbstractNrtm4IntegrationBase {

    @Autowired
    private NrtmFileProcessor nrtmFileProcessor;
    @Autowired
    private NrtmVersionInfoRepository nrtmVersionInfoRepository;
    @Autowired
    private NrtmSourceHolder nrtmSourceHolder;
    @Autowired
    private NrtmProcessControlJmx nrtmProcessControlJmx;

    @BeforeEach
    void beforeEach() {
        loadScripts(whoisTemplate, "nrtm_sample_sm.sql");
    }

    @Test
    void nrtm_write_job_is_disabled_by_jmx() {
        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();
        final var source = nrtmVersionInfoRepository.findLastVersion(nrtmSourceHolder.getSource());
        assertThat(source.isPresent(), is(false));
    }

    @Test
    void nrtm_write_job_is_enabled_by_jmx() {
        nrtmProcessControlJmx.enableInitialSnapshotGeneration();
        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();
        final var source = nrtmVersionInfoRepository.findLastVersion(nrtmSourceHolder.getSource());
        assertThat(source.isPresent(), is(true));
    }

}
