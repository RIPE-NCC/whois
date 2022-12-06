package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.persist.NrtmSourceHolder;
import net.ripe.db.nrtm4.publish.PublishableSnapshotFile;
import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static net.ripe.db.nrtm4.persist.NrtmDocumentType.SNAPSHOT;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.loadScripts;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.truncateTables;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;


@Tag("IntegrationTest")
@ContextConfiguration(locations = {"classpath:applicationContext-nrtm4-test.xml"})
public class NotificationFileGenerationServiceIntegrationTest extends AbstractDatabaseHelperIntegrationTest {

    @Autowired
    private NotificationFileGenerationService notificationFileGenerationService;

    @Autowired
    private NrtmSourceHolder nrtmSourceHolder;

    @BeforeEach
    public void setUp() {
        truncateTables(databaseHelper.getNrtmTemplate());
    }

    @Test
    public void snapshot_file_is_generated() {
        loadScripts(whoisTemplate, "nrtm_sample_sm.sql");
        final String sessionID;
        {
            final Optional<PublishableSnapshotFile> optFile = notificationFileGenerationService.generateSnapshot(nrtmSourceHolder.getSource());
            assertThat(optFile.isPresent(), is(true));
            final PublishableSnapshotFile snapshotFile = optFile.get();
            assertThat(snapshotFile.getVersion(), is(1L));
            sessionID = snapshotFile.getSessionID();
            assertThat(sessionID, is(notNullValue()));
            assertThat(snapshotFile.getSource(), is(nrtmSourceHolder.getSource()));
            assertThat(snapshotFile.getNrtmVersion(), is(4));
            assertThat(snapshotFile.getType(), is(SNAPSHOT));
        }
        {
            // don't increment snapshot version
            final Optional<PublishableSnapshotFile> snapshotFileOptional = notificationFileGenerationService.generateSnapshot(nrtmSourceHolder.getSource());
            assertThat(snapshotFileOptional.isPresent(), is(false));
        }
    }

}