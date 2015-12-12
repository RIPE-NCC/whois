package net.ripe.db.whois.scheduler.task.grs;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.scheduler.AbstractSchedulerIntegrationTest;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import javax.sql.DataSource;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
@DirtiesContext
public class GrsImporterTestIntegration extends AbstractSchedulerIntegrationTest {

    @Autowired
    private AuthoritativeResourceData authoritativeResourceData;
    @Autowired
    @Qualifier("internalsDataSource")
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    @BeforeClass
    public static void setUpClass() throws Exception {
        DatabaseHelper.addGrsDatabases("TEST-GRS");
    }

    @Before
    public void setUp() throws Exception {
        queryServer.start();
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    public void incremental_insert_autnum() throws Exception {
        assertThat(isMaintainedInRirSpace(ObjectType.AUT_NUM, "AS105"), is(false));

        insert("AS105-AS105");
        authoritativeResourceData.refreshAuthoritativeResourceCacheOnChange();

        assertThat(isMaintainedInRirSpace(ObjectType.AUT_NUM, "AS105"), is(true));
    }

    @Test
    public void incremental_remove_autnum() throws Exception {
        assertThat(isMaintainedInRirSpace(ObjectType.AUT_NUM, "AS102"), is(true));

        delete("AS102");
        authoritativeResourceData.refreshAuthoritativeResourceCacheOnChange();

        assertThat(isMaintainedInRirSpace(ObjectType.AUT_NUM, "AS102"), is(false));
    }

    @Test
    public void incremental_insert_and_remove_inetnum() throws Exception {
        delete("0.0.0.0/0");
        insert("193.0.0.0/8");
        authoritativeResourceData.refreshAuthoritativeResourceCacheOnChange();

        assertThat(isMaintainedInRirSpace(ObjectType.INETNUM, "193.0.0.1"), is(true));
        assertThat(isMaintainedInRirSpace(ObjectType.INETNUM, "10.0.0.1"), is(false));
    }

    // helper methods

    private boolean isMaintainedInRirSpace(final ObjectType objectType, final String pkey) {
        return authoritativeResourceData.getAuthoritativeResource(CIString.ciString("TEST")).isMaintainedInRirSpace(objectType, CIString.ciString(pkey));
    }

    private void delete(final String resource) {
        jdbcTemplate.update("DELETE from authoritative_resource WHERE resource = ?", resource);
    }

    private void insert(final String resource) {
        jdbcTemplate.update("INSERT INTO authoritative_resource (source, resource) values (?,?)", "test", resource);
    }
}
