package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.persist.NrtmSourceHolder;
import net.ripe.db.nrtm4.persist.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.publish.PublishableDeltaFile;
import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperIntegrationTest;
import net.ripe.db.whois.common.domain.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.loadScripts;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.truncateTables;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;


@Tag("IntegrationTest")
@ContextConfiguration(locations = {"classpath:applicationContext-nrtm4-test.xml"})
public class DeltaFileGeneratorIntegrationTest extends AbstractDatabaseHelperIntegrationTest {

    @Autowired
    private DeltaFileGenerator deltaFileGenerator;

    @Autowired
    private NrtmVersionInfoRepository versionDao;

    @Autowired
    private JsonSerializer jsonSerializer;

    @BeforeEach
    public void setUp() {
        truncateTables(databaseHelper.getNrtmTemplate());
        truncateTables(databaseHelper.getWhoisTemplate());
    }

    private void loadSerials() {
        loadScripts(whoisTemplate, "nrtm_sample_sm.sql");
        whoisTemplate.update("UPDATE last SET timestamp = ?", Timestamp.from(testDateTimeProvider.getCurrentDateTime()).getValue());
        whoisTemplate.update("UPDATE history SET timestamp = ?", Timestamp.from(testDateTimeProvider.getCurrentDateTime()).getValue());
    }

    private void insertFirstVersion() {
        versionDao.createInitialSnapshot(NrtmSourceHolder.valueOf("TEST"), 0);
    }

    @Test
    public void test_delta_file_cannot_be_generated() {
        assertThrows(IllegalStateException.class, () ->
            deltaFileGenerator.createDelta(NrtmSourceHolder.valueOf("TEST"))
        );
        insertFirstVersion();
        final var deltas = deltaFileGenerator.createDelta(NrtmSourceHolder.valueOf("TEST"));
        assertThat(deltas.isEmpty(), is(true));
    }

    @Test
    public void test_delta_file_generation() {

        insertFirstVersion();
        loadSerials();
        final Optional<PublishableDeltaFile> optDeltaFile = deltaFileGenerator.createDelta(NrtmSourceHolder.valueOf("TEST"));
        assertThat(optDeltaFile.isPresent(), is(true));
        final PublishableDeltaFile deltaFile = optDeltaFile.get();
        final String sampleSm = "" +
            "{\"nrtm_version\":4," +
            "\"type\":\"delta\"," +
            "\"source\":\"TEST\"," +
            "\"session_id\":\"\"," +
            "\"version\":2," +
            "\"changes\":[" +
            "{\"action\":\"add_modify\",\"object\":\"aut-num:        AS6\\nas-name:        AS2TEST\\ndescr:          Description\\norg:            ORG-TEST-RIPE\\nadmin-c:        DUMY-RIPE\\ntech-c:         DUMY-RIPE\\nmnt-by:         RIPE-NCC-TEST-MNT\\nmnt-by:         TEST-MNTNR\\nsource:         RIPE\\nremarks:        ****************************\\nremarks:        * THIS OBJECT IS MODIFIED\\nremarks:        * Please note that all data that is generally regarded as personal\\nremarks:        * data has been removed from this object.\\nremarks:        * To view the original object, please query the RIPE Database at:\\nremarks:        * http://www.ripe.net/whois\\nremarks:        ****************************\\n\"}," +
            "{\"action\":\"add_modify\",\"object\":\"inetnum:        195.77.187.144 - 195.77.187.151\\nnetname:        Netname\\ndescr:          Description\\ncountry:        es\\nadmin-c:        DUMY-RIPE\\ntech-c:         DUMY-RIPE\\nstatus:         ASSIGNED PA\\nmnt-by:         MAINT-AS3352\\nsource:         RIPE\\nremarks:        ****************************\\nremarks:        * THIS OBJECT IS MODIFIED\\nremarks:        * Please note that all data that is generally regarded as personal\\nremarks:        * data has been removed from this object.\\nremarks:        * To view the original object, please query the RIPE Database at:\\nremarks:        * http://www.ripe.net/whois\\nremarks:        ****************************\\n\"}," +
            "{\"action\":\"add_modify\",\"object\":\"aut-num:        AS6\\nas-name:        ASNAME\\ndescr:          Description\\norg:            ORG-TEST-RIPE\\nadmin-c:        DUMY-RIPE\\ntech-c:         DUMY-RIPE\\nmnt-by:         RIPE-NCC-TEST-MNT\\nmnt-by:         TEST-MNTNR\\nsource:         RIPE\\nremarks:        ****************************\\nremarks:        * THIS OBJECT IS MODIFIED\\nremarks:        * Please note that all data that is generally regarded as personal\\nremarks:        * data has been removed from this object.\\nremarks:        * To view the original object, please query the RIPE Database at:\\nremarks:        * http://www.ripe.net/whois\\nremarks:        ****************************\\n\"}," +
            "{\"action\":\"delete\",\"object_class\":\"AUT_NUM\",\"primary_key\":\"AS6\"}" +
            "]}";
        assertThat(jsonSerializer.process(deltaFile).replaceFirst("\"session_id\":\"[^\"]+\"", "\"session_id\":\"\""), is(sampleSm));
    }

}
