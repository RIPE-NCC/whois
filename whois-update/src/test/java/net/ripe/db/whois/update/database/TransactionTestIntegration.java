package net.ripe.db.whois.update.database;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.update.dao.AbstractDaoTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class TransactionTestIntegration extends AbstractDaoTest {
    @Autowired @Qualifier("sourceAwareDataSource") DataSource dataSource;

    @Test
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void isolation_level_is_read_committed_so_that_global_locks_work_as_expected() throws Exception {
        assertThat(new JdbcTemplate(dataSource).queryForObject("select @@tx_isolation", String.class), is("READ-COMMITTED"));
    }
}
