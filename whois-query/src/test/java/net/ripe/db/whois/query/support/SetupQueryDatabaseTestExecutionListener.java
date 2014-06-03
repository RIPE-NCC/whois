package net.ripe.db.whois.query.support;

import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.source.DefaultSourceContext;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class SetupQueryDatabaseTestExecutionListener extends AbstractTestExecutionListener {
    @Override
    public void beforeTestClass(final TestContext testContext) throws Exception {
        DatabaseHelper.setupDatabase();
    }

    @Override
    public void beforeTestMethod(final TestContext testContext) throws Exception {
        final ApplicationContext applicationContext = testContext.getApplicationContext();
        final DatabaseHelper databaseHelper = applicationContext.getBean(DatabaseHelper.class);
        final SourceContext sourceContext = applicationContext.getBean(DefaultSourceContext.class);

        sourceContext.setCurrent(Source.slave("TEST"));
        databaseHelper.setup();
    }

    @Override
    public void afterTestMethod(final TestContext testContext) throws Exception {
        final ApplicationContext applicationContext = testContext.getApplicationContext();
        final SourceContext sourceContext = applicationContext.getBean(DefaultSourceContext.class);
        sourceContext.removeCurrentSource();
    }
}
