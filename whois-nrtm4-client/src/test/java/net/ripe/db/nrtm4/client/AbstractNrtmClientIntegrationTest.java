package net.ripe.db.nrtm4.client;

import net.ripe.db.nrtm4.client.dao.Nrtm4ClientMirrorRepository;
import net.ripe.db.whois.common.support.AbstractDaoIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;

public class AbstractNrtmClientIntegrationTest extends AbstractDaoIntegrationTest {

    @Autowired
    private Nrtm4ClientMirrorRepository nrtm4ClientMirrorRepository;
}
