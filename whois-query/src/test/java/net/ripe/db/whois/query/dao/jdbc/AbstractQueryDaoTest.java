package net.ripe.db.whois.query.dao.jdbc;

import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperTest;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.support.SetupQueryDatabaseTestExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

@ContextConfiguration(locations = {"classpath:applicationContext-query-test.xml"})
@TestExecutionListeners(listeners = {
        SetupQueryDatabaseTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class},
        inheritListeners = false)
public abstract class AbstractQueryDaoTest extends AbstractDatabaseHelperTest {
    @Autowired SourceContext sourceContext;
}
