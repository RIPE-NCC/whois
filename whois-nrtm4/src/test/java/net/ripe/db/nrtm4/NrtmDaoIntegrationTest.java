package net.ripe.db.nrtm4;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;


@Tag("IntegrationTest")
@ContextConfiguration(locations = {"classpath:applicationContext-nrtm4-test.xml"})
public class NrtmDaoIntegrationTest extends AbstractNrtm4IntegrationBase {

    @Test
    public void firstVersionIsOne() {

    }
}
