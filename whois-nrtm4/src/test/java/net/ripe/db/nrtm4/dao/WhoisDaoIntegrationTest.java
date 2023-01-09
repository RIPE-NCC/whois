package net.ripe.db.nrtm4.dao;

import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperIntegrationTest;
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
public class WhoisDaoIntegrationTest extends AbstractDatabaseHelperIntegrationTest {

    @Autowired
    WhoisDao whoisDao;

    @Test
    public void prepared_query_gets_all_rows() {
        loadScripts(whoisTemplate, "nrtm_sample_sm.sql");

        final var objects = List.of(
            new ObjectData(11044887, 1),
            new ObjectData(11044888, 1),
            new ObjectData(5158, 2)
        );
        final var map = whoisDao.findRpslMapForObjects(objects);
        assertThat(map.size(), is(3));
    }

}
