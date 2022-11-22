package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.persist.DeltaFileModelRepository;
import net.ripe.db.nrtm4.persist.RpslObjectModel;
import net.ripe.db.nrtm4.persist.SerialModel;
import net.ripe.db.nrtm4.persist.WhoisSlaveDao;
import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperIntegrationTest;
import net.ripe.db.whois.common.domain.Timestamp;
import org.javatuples.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.loadScripts;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


@Tag("IntegrationTest")
@ContextConfiguration(locations = {"classpath:applicationContext-nrtm4-test.xml"})
public class NrtmProcessorIntegrationTest extends AbstractDatabaseHelperIntegrationTest {

    @Autowired
    private DeltaProcessor deltaProcessor;

    @Autowired
    private DeltaFileModelRepository deltaFileModelRepository;

    @Autowired
    private WhoisSlaveDao whoisSlaveDao;

    @BeforeEach
    public void before() {
        loadSerials();
    }

    private void loadSerials() {
        loadScripts(whoisTemplate, "nrtm_sample.sql");
        whoisTemplate.update("UPDATE last SET timestamp = ?", Timestamp.from(testDateTimeProvider.getCurrentDateTime()).getValue());
        whoisTemplate.update("UPDATE history SET timestamp = ?", Timestamp.from(testDateTimeProvider.getCurrentDateTime()).getValue());
    }

    @Test
    public void test_whois_slave_dao() {
        final List<Pair<SerialModel, RpslObjectModel>> changes = whoisSlaveDao.findSerialsAndObjectsSinceSerial(0);
        assertThat(changes.size(), is(64));
    }

}
