package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.persist.NrtmSourceHolder;
import net.ripe.db.nrtm4.persist.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.publish.PublishableSnapshotFile;
import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static net.ripe.db.nrtm4.persist.NrtmDocumentType.snapshot;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.truncateTables;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


@Tag("IntegrationTest")
@ContextConfiguration(locations = {"classpath:applicationContext-nrtm4-test.xml"})
public class NotificationFileGenerationServiceIntegrationTest extends AbstractDatabaseHelperIntegrationTest {

    @Autowired
    private NotificationFileGenerationService notificationFileGenerationService;

    @Autowired
    private NrtmVersionInfoRepository nrtmVersionInfoRepository;

    @Autowired
    private NrtmSourceHolder nrtmSourceHolder;

    @BeforeEach
    public void setUp() {
        truncateTables(databaseHelper.getNrtmTemplate());
    }

    @Test
    public void snapshot_file_is_generated() {
        final String sessionID;
        {
            final PublishableSnapshotFile publishableSnapshotFile = notificationFileGenerationService.generateSnapshot(nrtmSourceHolder.getSource());
            assertThat(publishableSnapshotFile.getVersion(), is(1L));
            sessionID = publishableSnapshotFile.getSessionID();
            assertThat(publishableSnapshotFile.getSource(), is(nrtmSourceHolder.getSource()));
            assertThat(publishableSnapshotFile.getNrtmVersion(), is(4));
            assertThat(publishableSnapshotFile.getType(), is(snapshot));
        }
        {
            final PublishableSnapshotFile publishableSnapshotFile = notificationFileGenerationService.generateSnapshot(nrtmSourceHolder.getSource());
            assertThat(publishableSnapshotFile.getVersion(), is(2L));
            assertThat(sessionID, is(publishableSnapshotFile.getSessionID()));
            assertThat(publishableSnapshotFile.getSource(), is(nrtmSourceHolder.getSource()));
            assertThat(publishableSnapshotFile.getNrtmVersion(), is(4));
            assertThat(publishableSnapshotFile.getType(), is(snapshot));
        }
        {
            final PublishableSnapshotFile publishableSnapshotFile = notificationFileGenerationService.generateSnapshot(nrtmSourceHolder.getSource());
            assertThat(publishableSnapshotFile.getVersion(), is(3L));
            assertThat(sessionID, is(publishableSnapshotFile.getSessionID()));
            assertThat(publishableSnapshotFile.getSource(), is(nrtmSourceHolder.getSource()));
            assertThat(publishableSnapshotFile.getNrtmVersion(), is(4));
            assertThat(publishableSnapshotFile.getType(), is(snapshot));
        }
    }

}