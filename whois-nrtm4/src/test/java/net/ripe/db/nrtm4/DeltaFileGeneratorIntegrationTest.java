package net.ripe.db.nrtm4;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import net.ripe.db.nrtm4.persist.NrtmSourceHolder;
import net.ripe.db.nrtm4.persist.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.publish.PublishableDeltaFile;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperIntegrationTest;
import net.ripe.db.whois.common.domain.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

import javax.sql.DataSource;
import java.util.Optional;

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.loadScripts;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.truncateTables;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


@Tag("IntegrationTest")
@ContextConfiguration(locations = {"classpath:applicationContext-nrtm4-test.xml"})
public class DeltaFileGeneratorIntegrationTest extends AbstractDatabaseHelperIntegrationTest {

    @Autowired
    private DeltaFileGenerator deltaFileGenerator;

    @Autowired
    @Qualifier("nrtmDataSource")
    private DataSource dataSource;

    @Mock
    private NrtmFileUtil nrtmFileUtil;

    private NrtmVersionInfoRepository versionDao;

    @BeforeEach
    public void setUp() {
        truncateTables(databaseHelper.getNrtmTemplate());
        truncateTables(databaseHelper.getWhoisTemplate());
        MockitoAnnotations.openMocks(this);
        versionDao = new NrtmVersionInfoRepository(dataSource, nrtmFileUtil);
    }

    private void loadSerials() {
        loadScripts(whoisTemplate, "nrtm_sample_sm.sql");
        whoisTemplate.update("UPDATE last SET timestamp = ?", Timestamp.from(testDateTimeProvider.getCurrentDateTime()).getValue());
        whoisTemplate.update("UPDATE history SET timestamp = ?", Timestamp.from(testDateTimeProvider.getCurrentDateTime()).getValue());
    }

    private void insertFirstVersion() {
        when(nrtmFileUtil.sessionId()).thenReturn("1234567890");
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
    public void test_delta_file_generation() throws JsonProcessingException {

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
        final JsonMapper objectMapper = JsonMapper.builder().build();
        assertThat(objectMapper.writeValueAsString(deltaFile).replaceFirst("\"session_id\":\"[^\"]+\"", "\"session_id\":\"\""), is(sampleSm));
        assertThat(deltaFile.getSha256hex(), is("c875b8c4eb164a3049f7cee0db494a0504febe11506bc4ceb3f644ef0ea00283"));
        assertThat(deltaFile.getFileName(), startsWith("nrtm-delta.2."));
    }

}
