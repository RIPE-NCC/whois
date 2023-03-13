package net.ripe.db.nrtm4;

import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperIntegrationTest;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration(locations = {"classpath:applicationContext-nrtm4-test.xml"})
public class AbstractNrtm4IntegrationBase extends AbstractDatabaseHelperIntegrationTest {

    @BeforeAll
    static void setProperties() {
        System.setProperty("whois.source", "TEST");
        System.setProperty("whois.nonauth.source", "TEST-NONAUTH");
    }

}
