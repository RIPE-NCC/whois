package net.ripe.db.nrtm4.persist;

import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.truncateTables;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;


@Tag("IntegrationTest")
@ContextConfiguration(locations = {"classpath:applicationContext-nrtm4-test.xml"})
public class NrtmDaoIntegrationTest extends AbstractDatabaseHelperIntegrationTest {

    @Autowired
    private NrtmVersionDao nrtmVersionDao;

    @BeforeEach
    public void setUp() {
        truncateTables(databaseHelper.getNrtmTemplate());
    }

    @Test
    public void result_is_not_present_when_source_is_not_populated() {
        final Optional<VersionInformation> version = nrtmVersionDao.findLastVersion(NrtmSource.RIPE);
        assertThat(version.isPresent(), is(false));
    }

    @Test
    public void source_is_unique() {
        nrtmVersionDao.createNew(NrtmSource.RIPE);
        final Exception thrown = assertThrows(
                DuplicateKeyException.class,
                () -> nrtmVersionDao.createNew(NrtmSource.RIPE),
                "Expected nrtmVersionDao.createNew(...) to throw DuplicateKeyException"
        );
        assertThat(thrown.getMessage(), containsString("Duplicate entry 'RIPE'"));
    }

    @Test
    public void first_version_is_one() {
        nrtmVersionDao.createNew(NrtmSource.RIPE);
        final Optional<VersionInformation> version = nrtmVersionDao.findLastVersion(NrtmSource.RIPE);
        assertThat(version.isPresent(), is(true));
        assertThat(version.get().getSource(), is(NrtmSource.RIPE));
        assertThat(version.get().getVersion(), is(1L));
    }

}
