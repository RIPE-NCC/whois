package net.ripe.db.nrtm4.client;

import net.ripe.db.nrtm4.client.dao.Nrtm4ClientMirrorRepository;
import net.ripe.db.nrtm4.client.reader.UpdateNotificationFileReader;
import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperIntegrationTest;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(locations = {"classpath:applicationContext-nrtm4-client-test.xml"})
public class AbstractNrtmClientIntegrationTest extends AbstractDatabaseHelperIntegrationTest {

    @Autowired
    protected Nrtm4ClientMirrorRepository nrtm4ClientMirrorRepository;

    @Autowired
    protected UpdateNotificationFileReader updateNotificationFileReader;

    @BeforeAll
    public static void setUp(){
        System.setProperty("nrtm4.client.enabled", "true");
    }


}
