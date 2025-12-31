package net.ripe.db.whois.common.support;

import net.ripe.db.whois.common.WhoisCommonTestConfiguration;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperIntegrationTest;
import net.ripe.db.whois.common.iptree.IpTreeUpdater;
import net.ripe.db.whois.common.source.SourceAwareDataSource;
import net.ripe.db.whois.common.source.SourceContext;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {WhoisCommonTestConfiguration.class})
public abstract class AbstractDaoIntegrationTest extends AbstractDatabaseHelperIntegrationTest {
    @Autowired protected SourceContext sourceContext;
    @Autowired protected RpslObjectDao rpslObjectDao;
    @Autowired protected RpslObjectUpdateDao rpslObjectUpdateDao;
    @Autowired protected SourceAwareDataSource sourceAwareDataSource;
    @Autowired protected IpTreeUpdater ipTreeUpdater;

    @BeforeEach
    public void resetIpTrees() {
        ipTreeUpdater.rebuild();
    }
}
