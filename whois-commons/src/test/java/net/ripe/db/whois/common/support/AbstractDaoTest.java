package net.ripe.db.whois.common.support;

import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperTest;
import net.ripe.db.whois.common.iptree.IpTreeUpdater;
import net.ripe.db.whois.common.source.SourceAwareDataSource;
import net.ripe.db.whois.common.source.SourceContext;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(locations = {"classpath:applicationContext-commons-test.xml"})
public abstract class AbstractDaoTest extends AbstractDatabaseHelperTest {
    @Autowired protected SourceContext sourceContext;
    @Autowired protected RpslObjectDao rpslObjectDao;
    @Autowired protected RpslObjectUpdateDao rpslObjectUpdateDao;
    @Autowired protected SourceAwareDataSource sourceAwareDataSource;
    @Autowired protected IpTreeUpdater ipTreeUpdater;

    @Before
    public void resetIpTrees() {
        ipTreeUpdater.rebuild();
    }
}
